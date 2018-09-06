package io.sikorka.android.core.ethereumclient

import arrow.core.Either
import arrow.core.Try
import io.sikorka.android.core.TransactionNotFoundException
import io.sikorka.android.core.model.TransactionReceipt
import io.sikorka.android.core.model.converters.GethReceiptConverter
import org.ethereum.geth.BoundContract
import org.ethereum.geth.Context
import org.ethereum.geth.EthereumClient
import org.ethereum.geth.Geth
import java.math.BigDecimal
import io.sikorka.android.core.model.Address as SikorkaAddress

class LightClient(
  private val ethereumClient: EthereumClient,
  private val context: Context
) {
  private val receiptConverter = GethReceiptConverter()

  fun getTransactionReceipt(txHashHex: String): Either<Throwable, TransactionReceipt> {
    return Try {
      val hash = Geth.newHashFromHex(txHashHex)
      val receipt = ethereumClient.getTransactionReceipt(context, hash)
      return@Try receiptConverter.convert(receipt)
    }.toEither()
      .mapLeft {
        val message = it.message ?: ""
        return@mapLeft if (message.contains("not found", true)) {
          TransactionNotFoundException(txHashHex, it)
        } else {
          it
        }
      }
  }

  /**
   * Requests the balance for the specified account.
   */
  fun getBalance(address: SikorkaAddress): BigDecimal {
    val accountAddress = Geth.newAddressFromHex(address.hex)
    val bigIntBalance = ethereumClient.getBalanceAt(context, accountAddress, -1)
    return BigDecimal(bigIntBalance.getString(10))
  }

  fun <T> bindContract(address: String, abi: String, creator: (contract: BoundContract) -> T): T {
    return creator(Geth.bindContract(Geth.newAddressFromHex(address), abi, ethereumClient))
  }
}