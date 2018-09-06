package io.sikorka.android.ui.accounts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import arrow.core.Try
import io.sikorka.android.core.accounts.AccountRepository
import io.sikorka.android.core.accounts.AccountsModel
import io.sikorka.android.core.model.Account
import io.sikorka.android.utils.schedulers.AppDispatchers
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

class AccountViewModel(
  private val accountRepository: AccountRepository,
  private val dispatchers: AppDispatchers
) : ViewModel() {
  private var async: Deferred<Unit>? = null
  private val accountData: MutableLiveData<AccountsModel> = MutableLiveData()

  fun accounts(): LiveData<AccountsModel> {
    async = async(dispatchers.io) {
      update()
    }
    return accountData
  }

  override fun onCleared() {
    super.onCleared()
    async?.cancel()
  }

  private suspend fun update() {
    val fold = Try {
      accountRepository.accounts()
    }.fold({ return@fold AccountsModel("", emptyList()) }, { return@fold it })
    accountData.postValue(fold)
  }

  fun setDefault(account: Account) {
    launch(dispatchers.io) {
      accountRepository.setDefaultAccount(account)
      update()
    }
  }

  fun deleteAccount(account: Account, passphrase: String) {
    require(passphrase.isBlank()) { "Passphrase cannot be blank" }

    launch(dispatchers.io) {
      accountRepository.deleteAccount(account, passphrase)
    }

  }
}