package io.sikorka.android.ui.accounts.accountcreation

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.textfield.TextInputLayout
import io.sikorka.android.R
import io.sikorka.android.core.accounts.ValidationResult
import io.sikorka.android.core.accounts.ValidationResult.Code
import io.sikorka.android.data.Result
import io.sikorka.android.ui.asString
import io.sikorka.android.ui.coloredSpan
import kotlinx.coroutines.experimental.launch
import org.koin.android.ext.android.inject

class AccountCreationDialog : DialogFragment() {

  private lateinit var passphraseField: EditText
  private lateinit var passphraseConfirmationField: EditText
  private lateinit var passphraseInput: TextInputLayout
  private lateinit var passphraseConfirmationInput: TextInputLayout

  private val viewModel: AccountCreationDialogViewModel by inject()

  private lateinit var dialog: AlertDialog

  private val passphrase: String
    get() = passphraseField.asString()

  private val passphraseConfirmation: String
    get() = passphraseConfirmationField.asString()

  private lateinit var onDismissAction: (() -> Unit)
  private lateinit var fm: FragmentManager

  @SuppressLint("InflateParams")
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val inflater = LayoutInflater.from(requireContext())
    val view = inflater.inflate(R.layout.dialog__account_create, null, false)
    view.run {
      passphraseField = findViewById(R.id.accounts__passphrase)
      passphraseConfirmationField = findViewById(R.id.accounts__passphrase_confirmation)
      passphraseInput = findViewById(R.id.accounts__passphrase_input)
      passphraseConfirmationInput = findViewById(R.id.accounts__passphrase_confirmation_input)
    }

    val builder = AlertDialog.Builder(requireContext())
      .setTitle(coloredSpan(R.string.account__create_account))
      .setView(view)
      .setCancelable(false)
      .setPositiveButton(coloredSpan(android.R.string.ok)) { _, _ ->
        launch {
          val result = viewModel.createAccount(passphrase, passphraseConfirmation)
          when (result) {
            is Result.Success<*> -> complete()
            is Result.Failure<*> -> showError(result.code)
          }
        }
      }
      .setNegativeButton(coloredSpan(android.R.string.cancel)) { dialog, _ -> dialog.dismiss() }

    dialog = builder.create()
    return dialog
  }

  private fun showError(@Code code: Int) {
    clearErrors()
    when (code) {
      ValidationResult.CONFIRMATION_MISMATCH -> {
        val errorMessage = getString(R.string.account_creation__passphrase_missmatch)
        passphraseConfirmationInput.error = errorMessage
      }
      ValidationResult.EMPTY_PASSPHRASE -> {
        val errorMessage = getString(R.string.account_creation__passphrase_empty)
        passphraseInput.error = errorMessage
      }
      ValidationResult.PASSWORD_SHORT -> {
        val errorMessage = getString(R.string.account_creation__password_short, 8)
        passphraseInput.error = errorMessage
      }
    }
  }

  private fun complete() {
    onDismissAction.invoke()
    dialog.dismiss()
  }

  private fun clearErrors() {
    passphraseInput.error = null
    passphraseConfirmationInput.error = null
  }

  fun show() {
    show(fm, TAG)
  }

  override fun dismiss() {
    dialog.dismiss()
  }

  companion object {
    const val TAG = "account_creation_dialog"

    fun newInstance(fm: FragmentManager, action: (() -> Unit)): AccountCreationDialog {
      val creationDialog = AccountCreationDialog()
      creationDialog.fm = fm
      creationDialog.onDismissAction = action
      return creationDialog
    }
  }
}