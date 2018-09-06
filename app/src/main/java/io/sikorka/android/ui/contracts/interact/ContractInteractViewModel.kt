package io.sikorka.android.ui.contracts.interact

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.sikorka.android.contract.DiscountContract
import io.sikorka.android.core.GethNode
import io.sikorka.android.core.contracts.model.ContractGas
import io.sikorka.android.data.contracts.BoundContractData
import io.sikorka.android.data.contracts.ContractRepository
import io.sikorka.android.data.transactions.PendingTransaction
import io.sikorka.android.data.transactions.PendingTransactionDao
import io.sikorka.android.helpers.hexStringToByteArray
import io.sikorka.android.utils.schedulers.AppSchedulers
import org.ethereum.geth.Address
import org.threeten.bp.Instant.now
import timber.log.Timber
import java.math.BigInteger

class ContractInteractViewModel(
  private val contractRepository: ContractRepository,
  private val appSchedulers: AppSchedulers,
  private val gethNode: GethNode,
  private val pendingTransactionDao: PendingTransactionDao
) : ViewModel() {

  private var gas: ContractGas? = null
  private var passphrase: String? = null
  private var detectorMessage: String? = null

  private val contractData: MutableLiveData<BoundContractData> = MutableLiveData()

  fun cacheGas(gas: ContractGas) {
    this.gas = gas
  }

  fun cachePassPhrase(passphrase: String) {
    this.passphrase = passphrase
  }

  fun cacheMessage(detectorSignedMessage: String?) {
    this.detectorMessage = detectorSignedMessage
  }

  private lateinit var boundInterface: DiscountContract
  private var usesDetector: Boolean = false

  fun load(contractAddress: String) {
    contractData.postValue(contractRepository.boundContract(contractAddress))
  }

  fun verify() {

    val data = if (usesDetector) {
      detectorMessage?.hexStringToByteArray() ?: kotlin.ByteArray(0)
    } else {
      kotlin.ByteArray(0)
    }

//    ifNotNull(gas, passphrase) { gas, passphrase ->
//      disposables += contractRepository.transact({
//        boundInterface.claimToken(it, data)
//      }, passphrase, gas)
//        .subscribeOn(appSchedulers.io)
//        .observeOn(appSchedulers.main)
//        .subscribe({
//          attachedView().showConfirmationResult(true)
//          pendingTransactionDao.insert(PendingTransaction(
//            txHash = it.hash.hex,
//            dateAdded = now().epochSecond
//          ))
//        }) {
//          Timber.e(it, "failed")
//        }
//    }
  }

  fun startClaimFlow() {
    if (usesDetector) {
//      attachedView().startDetectorFlow()
    } else {
      prepareGasSelection()
    }
  }

  private fun ifNotNull(
    gas: ContractGas?,
    passphrase: String?,
    action: (gas: ContractGas, passphrase: String) -> Unit
  ): Boolean {
    return if (gas != null && passphrase != null) {
      action(gas, passphrase)
      true
    } else {
      false
    }
  }

  fun prepareGasSelection() {
    gethNode.suggestedGasPrice()
  }

  private fun Address.toInt(): Int {
    val hex = hex.replace("0x", "")
    val integer = BigInteger(hex, 16)
    return integer.toInt()
  }
}