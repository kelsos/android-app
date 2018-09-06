package io.sikorka.android.ui.wizard

import androidx.lifecycle.ViewModel
import arrow.core.Try
import io.sikorka.android.core.accounts.AccountRepository

class WizardViewModel(
  private val accountRepository: AccountRepository
) : ViewModel() {
  fun checkForDefaultAccount(): Boolean {
    return Try { accountRepository.accountsExist() }.fold({ false }, { it })
  }
}