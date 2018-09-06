package io.sikorka.android.ui.accounts.accountimport

import androidx.lifecycle.ViewModel
import io.sikorka.android.core.accounts.AccountRepository
import io.sikorka.android.core.accounts.InvalidPassphraseException
import io.sikorka.android.core.accounts.PassphraseValidator
import io.sikorka.android.core.accounts.ValidationResult
import io.sikorka.android.core.model.Account
import io.sikorka.android.data.Result
import io.sikorka.android.io.toByteArray
import io.sikorka.android.ui.accounts.accountimport.AccountImportCodes.FAILED_TO_UNLOCK
import java.io.File

class AccountImportViewModel(
  private val accountRepository: AccountRepository,
  private val passphraseValidator: PassphraseValidator
) : ViewModel() {

  suspend fun import(
    filePath: String,
    filePassphrase: String,
    accountPassphrase: String,
    accountPassphraseConfirmation: String
  ): Result<Account> {

    val file = File(filePath)

    if (!file.exists()) {
      return Result.Failure(AccountImportCodes.FILE_DOES_NOT_EXIST)
    }

    val code = passphraseValidator.validate(accountPassphrase, accountPassphraseConfirmation)

    if (code != ValidationResult.OK) {
      return Result.Failure(code)
    }

    val key = file.toByteArray()
    return accountRepository.importAccount(key, filePassphrase, accountPassphrase)
      .fold({
        return@fold Result.Failure(if (it is InvalidPassphraseException) {
          FAILED_TO_UNLOCK
        } else {
          Result.Codes.GENERIC_FAILURE
        })
      }, {
        Result.Success(it)
      })
  }
}