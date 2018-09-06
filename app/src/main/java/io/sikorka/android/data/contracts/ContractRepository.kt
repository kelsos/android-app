package io.sikorka.android.data.contracts

import androidx.lifecycle.LiveData
import io.sikorka.android.contract.DiscountContract
import io.sikorka.android.contract.SikorkaRegistry
import io.sikorka.android.core.GethNode
import io.sikorka.android.core.accounts.AccountRepository
import io.sikorka.android.core.contracts.model.ContractGas
import io.sikorka.android.core.contracts.model.DetectorContractData
import io.sikorka.android.core.contracts.model.IContractData
import io.sikorka.android.data.contracts.deployed.DeployedSikorkaContract
import io.sikorka.android.data.contracts.deployed.DeployedSikorkaContractDao
import io.sikorka.android.data.contracts.pending.PendingContract
import io.sikorka.android.data.contracts.pending.PendingContractDao
import io.sikorka.android.utils.schedulers.AppDispatchers
import kotlinx.coroutines.experimental.withContext
import org.ethereum.geth.Address
import org.ethereum.geth.BigInt
import org.ethereum.geth.EthereumClient
import org.ethereum.geth.Geth
import org.ethereum.geth.TransactOpts
import org.ethereum.geth.Transaction
import org.threeten.bp.Instant
import timber.log.Timber
import java.math.BigDecimal

class ContractRepository(
  private val gethNode: GethNode,
  private val accountRepository: AccountRepository,
  private val pendingContractDao: PendingContractDao,
  private val deployedSikorkaContractDao: DeployedSikorkaContractDao,
  private val dispatchers: AppDispatchers
) {

  fun getDeployedContracts(): LiveData<List<DeployedSikorkaContract>> {
    return deployedSikorkaContractDao.getDeployedContracts()
  }

  suspend fun deployContract(passphrase: String, data: IContractData): DiscountContract {
    val signer = signer(passphrase, data.gas)
    val client = gethNode.ethereumClient()
    val deployData = DeployData(signer, client)
    return deploy(data, deployData)
  }

  private fun deploy(data: IContractData, deployData: DeployData): DiscountContract {
    val modifier = BigDecimal(COORDINATES_MODIFIER)
    val biLatitude = BigDecimal(data.latitude).multiply(modifier).toBigInteger()
    val biLogitude = BigDecimal(data.longitude).multiply(modifier).toBigInteger()

    val latitude = Geth.newBigInt(0).apply {
      setString(biLatitude.toString(10), 10)
    }

    val longitude = Geth.newBigInt(0).apply {
      setString(biLogitude.toString(10), 10)
    }

    val totalSupply = Geth.newBigInt(data.totalSupply)

    Timber.v("Settings lat: ${latitude.string()}, long: ${longitude.string()}")

    val secondsAllowed: BigInt?
    val detector: Address?
    if (data is DetectorContractData) {
      secondsAllowed = Geth.newBigInt(data.secondsAllowed.toLong())
      detector = Geth.newAddressFromHex(data.detectorAddress)
    } else {
      secondsAllowed = Geth.newBigInt(0)
      detector = Geth.newAddressFromHex("0000000000000000000000000000000000000000")
    }

    val registry = Geth.newAddressFromHex(SikorkaRegistry.REGISTRY_ADDRESS)
    val contract = DiscountContract.deploy(
      deployData.transactOpts,
      deployData.ec,
      data.name,
      detector,
      latitude,
      longitude,
      secondsAllowed,
      registry,
      totalSupply
    )

    val address = contract.address
    val deployer = contract.deployer
    val pendingContract = PendingContract(
      contractAddress = address.hex,
      transactionHash = deployer!!.hash.hex,
      dateCreated = Instant.now().epochSecond
    )
    Timber.v("pending contract: $pendingContract")
    pendingContractDao.insert(pendingContract)
    return contract
  }

  private suspend fun signer(passphrase: String, gas: ContractGas): TransactOpts {
    val account = accountRepository.selectedAccount()
    return gethNode.createTransactOpts(account, gas) { _, transaction, chainId ->
      val signedTransaction = accountRepository.sign(
        account.addressHex,
        passphrase,
        transaction,
        chainId
      )
      return@createTransactOpts checkNotNull(signedTransaction) {
        "null transaction was returned"
      }.also {
        Timber.v("signing ${it.hash.hex} ${it.cost} ${it.nonce}")
      }
    }
  }

  private data class DeployData(val transactOpts: TransactOpts, val ec: EthereumClient)

  suspend fun transact(
    function: (opts: TransactOpts) -> Transaction,
    passphrase: String,
    gas: ContractGas
  ): Transaction {
    return withContext(dispatchers.io) {
      function(signer(passphrase, gas))
    }
  }

  fun boundContract(addressHex: String): BoundContractData {

    val ec = gethNode.ethereumClient()
    val address = Geth.newAddressFromHex(addressHex)
    val boundContract = DiscountContract(address, ec)
    Timber.v("contract -> name ${boundContract.name()}")

    val detector = boundContract.detector()

    return BoundContractData(
      addressHex,
      boundContract.name(),
      detector.hex.isNullOrBlank(),
      detector.hex
    )

  }

  companion object {
    const val COORDINATES_MODIFIER = 10_000_000_000_000_000
  }
}