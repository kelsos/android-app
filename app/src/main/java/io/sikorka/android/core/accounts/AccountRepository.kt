package io.sikorka.android.core.accounts

import androidx.lifecycle.LiveData
import arrow.core.Either
import arrow.core.Try
import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import io.sikorka.android.core.accounts.AccountModel.Companion.NO_BALANCE
import io.sikorka.android.core.all
import io.sikorka.android.core.model.Address
import io.sikorka.android.core.model.converters.GethAccountConverter
import io.sikorka.android.core.model.converters.GethAddressConverter
import io.sikorka.android.data.balance.AccountBalance
import io.sikorka.android.data.balance.AccountBalanceDao
import io.sikorka.android.settings.AppPreferences
import io.sikorka.android.utils.schedulers.AppDispatchers
import kotlinx.coroutines.experimental.withContext
import org.ethereum.geth.Account
import org.ethereum.geth.BigInt
import org.ethereum.geth.Geth
import org.ethereum.geth.KeyStore
import org.ethereum.geth.Transaction
import timber.log.Timber
import io.sikorka.android.core.model.Account as SikorkaAccount

class AccountRepository(
  keystorePath: String,
  private val appPreferences: AppPreferences,
  private val accountBalanceDao: AccountBalanceDao,
  private val dispatchers: AppDispatchers
) {

  private val accountConverter: GethAccountConverter = GethAccountConverter()
  private val addressConverter: GethAddressConverter = GethAddressConverter()

  private val keystore = KeyStore(keystorePath, Geth.LightScryptN, Geth.LightScryptP)

  suspend fun createAccount(passphrase: String): Try<Account> {
    return withContext(dispatchers.io) {
      Try.invoke {
        val newAccount = keystore.newAccount(passphrase)
        setDefault(newAccount.address.hex)
        return@invoke newAccount
      }
    }
  }

  suspend fun accounts(): AccountsModel {
    return withContext(dispatchers.io) {
      val accounts = keystore.accounts.all().map { accountConverter.convert(it) }
      return@withContext AccountsModel(appPreferences.selectedAccount(), accounts.toList())
    }
  }

  suspend fun selectedAccount(): AccountModel {
    return withContext(dispatchers.io) {
      return@withContext Try {
        val addressHex = appPreferences.selectedAccount()
        val balance = accountBalanceDao.getBalance(addressHex)
        return@Try AccountModel(addressHex, balance?.balance ?: NO_BALANCE)
      }.fold({
        val addressHex = appPreferences.selectedAccount()
        return@fold AccountModel(addressHex)
      }, { it })
    }


  }

  private fun getAccountByHex(addressHex: String): Account {
    return keystore.accounts.all().first { it.address.hex.equals(addressHex, ignoreCase = true) }
  }

  suspend fun export(
    addressHex: String,
    passphrase: String,
    keyPassphrase: String
  ): Either<Throwable, ByteArray> {
    return withContext(dispatchers.io) {
      Try.invoke {
        keystore.exportKey(getAccountByHex(addressHex), passphrase, keyPassphrase)
      }.toEither().mapLeft { checkIfInvalidPassphrase(it) }
    }
  }

  private fun checkIfInvalidPassphrase(throwable: Throwable): Throwable {
    val message = throwable.message.orEmpty()
    return if (message.contains(INVALID_PASSPHRASE)) {
      InvalidPassphraseException(throwable)
    } else {
      throwable
    }
  }

  suspend fun deleteAccount(account: SikorkaAccount, passphrase: String): Either<Throwable, Unit> {
    return withContext(dispatchers.io) {
      Try {
        val matchingAccount = keystore.accounts
          .all()
          .first { it.address.hex == account.address.hex }

        keystore.deleteAccount(matchingAccount, passphrase)
        accountBalanceDao.deleteByHex(account.address.hex)
      }.toEither().mapLeft { checkIfInvalidPassphrase(it) }
    }
  }

  suspend fun importAccount(
    key: ByteArray,
    keyPassphrase: String,
    passphrase: String
  ): Either<Throwable, io.sikorka.android.core.model.Account> {

    return Try {
      val account = withContext(dispatchers.io) {
        val account = keystore.importKey(key, keyPassphrase, passphrase)
        setDefault(account.address.hex)
        return@withContext account
      } ?: throw InvalidPassphraseException()

      return@Try accountConverter.convert(account)
    }.toEither()
      .mapLeft { throwable -> checkIfInvalidPassphrase(throwable) }

  }

  private fun setDefault(accountHex: String) {
    if (appPreferences.selectedAccount().isBlank()) {
      appPreferences.selectAccount(accountHex)
    }
  }

  fun changePassphrase(account: Account, oldPassphrase: String, newPassphrase: String) {
    keystore.updateAccount(account, oldPassphrase, newPassphrase)
  }

  fun sign(
    address: String,
    passphrase: String,
    transaction: Transaction,
    chainId: BigInt
  ): Transaction? {
    val account = keystore.accounts.all()
      .first { it.address.hex.equals(address, ignoreCase = true) }
    val hex = account.address.hex
    Timber.v("Signing $hex - ${transaction.hash.hex} - chain: ${chainId.int64}")
    return keystore.signTxPassphrase(account, passphrase, transaction, chainId)
  }

  fun accountsExist(): Boolean {
    return keystore.accounts.size() > 0
  }

  suspend fun setDefaultAccount(account: SikorkaAccount) {
    withContext(dispatchers.io) {
      val accountHex = account.address.hex
      appPreferences.selectAccount(accountHex)
    }
  }

  fun getAccountAddresses(): Observable<Address> = keystore.accounts.all()
    .map { addressConverter.convert(it.address) }
    .toObservable()

  fun observeDefaultAccountBalance(): LiveData<AccountBalance> {
    return accountBalanceDao.observeBalance(appPreferences.selectedAccount())
  }

  companion object {
    private const val INVALID_PASSPHRASE = "could not decrypt key with given passphrase"
  }
}