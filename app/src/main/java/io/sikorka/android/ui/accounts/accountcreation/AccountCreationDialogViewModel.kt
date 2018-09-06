package io.sikorka.android.ui.accounts.accountcreation

import androidx.lifecycle.ViewModel
import io.sikorka.android.core.accounts.AccountRepository
import io.sikorka.android.core.accounts.PassphraseValidator
import io.sikorka.android.core.accounts.ValidationResult
import io.sikorka.android.data.Result

class AccountCreationDialogViewModel(
  private val accountRepository: AccountRepository,
  private val passphraseValidator: PassphraseValidator
) : ViewModel() {

  suspend fun createAccount(passphrase: String, passphraseConfirmation: String): Result<Boolean> {
    val code = passphraseValidator.validate(passphrase, passphraseConfirmation)

    if (code != ValidationResult.OK) {
      return Result.Failure(code)
    }
    return accountRepository.createAccount(passphrase)
      .fold({
        return@fold Result.Failure()
      }, {
        return@fold Result.Success(true)
      })
  }
}