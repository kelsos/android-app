package io.sikorka.android.ui.wizard.slides.accountsetup

import androidx.lifecycle.ViewModel
import io.sikorka.android.settings.AppPreferences

class AccountSetupViewModel(
  private val appPreferences: AppPreferences
) : ViewModel() {
  fun account(): String {
    return appPreferences.selectedAccount()
  }
}