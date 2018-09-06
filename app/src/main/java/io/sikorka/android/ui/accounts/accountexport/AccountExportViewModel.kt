package io.sikorka.android.ui.accounts.accountexport

import androidx.lifecycle.ViewModel
import io.sikorka.android.core.accounts.AccountRepository
import io.sikorka.android.core.accounts.InvalidPassphraseException
import io.sikorka.android.core.accounts.PassphraseValidator
import io.sikorka.android.core.accounts.ValidationResult
import io.sikorka.android.data.Result
import io.sikorka.android.io.toFile
import java.io.File

class AccountExportViewModel(
  private val accountRepository: AccountRepository,
  private val passphraseValidator: PassphraseValidator
) : ViewModel() {

  suspend fun export(
    accountHex: String,
    passphrase: String,
    encryptionPass: String,
    confirmation: String,
    path: String
  ): Result<Boolean> {
    val exportDirectory = File(path)

    if (!exportDirectory.exists()) {
      exportDirectory.mkdir()
    }

    if (accountHex.isBlank()) {
      error("Account should never be blank")
    }

    if (passphrase.isBlank()) {
      return Result.Failure(AccountExportCodes.ACCOUNT_PASSPHRASE_EMPTY)
    }

    val code = passphraseValidator.validate(encryptionPass, confirmation)
    if (code != ValidationResult.OK) {
      return Result.Failure(code)
    }

    return accountRepository.export(accountHex, passphrase, encryptionPass).fold({
      return@fold Result.Failure(if (it is InvalidPassphraseException) {
        AccountExportCodes.INVALID_PASSPHRASE
      } else {
        Result.Codes.GENERIC_FAILURE
      })
    }, {
      return@fold if (it.isEmpty()) {
        Result.Failure(AccountExportCodes.FAILED_TO_UNLOCK_ACCOUNT)
      } else {
        it.toFile(File(exportDirectory, accountHex))
        Result.Success(true)
      }
    })
  }
}