package io.sikorka.android.ui.wizard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.github.paolorotolo.appintro.AppIntro2
import io.sikorka.android.R
import io.sikorka.android.SikorkaService
import io.sikorka.android.ui.dialogs.showInfo
import io.sikorka.android.ui.main.MainActivity
import io.sikorka.android.ui.wizard.slides.InformationFragment
import io.sikorka.android.ui.wizard.slides.accountsetup.AccountSetupFragment
import io.sikorka.android.ui.wizard.slides.networkselection.NetworkSelectionFragment
import org.koin.android.ext.android.inject

class WizardActivity : AppIntro2() {

  private val presenter: WizardViewModel by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    askForPermissions(arrayOf(
      Manifest.permission.READ_EXTERNAL_STORAGE,
      Manifest.permission.WRITE_EXTERNAL_STORAGE,
      Manifest.permission.ACCESS_FINE_LOCATION
    ), 2)

    setNavBarColor(R.color.colorPrimaryDark)

    addSlide(InformationFragment.newInstance())
    addSlide(NetworkSelectionFragment.newInstance())
    addSlide(AccountSetupFragment.newInstance())
    progressButtonEnabled = true
    skipButtonEnabled = false
    setNextPageSwipeLock(false)
    setSwipeLock(false)
    setIndicatorColor(R.color.colorAccent, R.color.colorAccentLight)
  }

  override fun onDonePressed(currentFragment: androidx.fragment.app.Fragment?) {
    accountsExists(presenter.checkForDefaultAccount())
  }

  fun accountsExists(exists: Boolean) {
    if (exists) {
      done()
    } else {
      showInfo(R.string.wizard__no_accounts_title, R.string.wizard__no_accounts_content)
    }
  }

  private fun done() {
    SikorkaService.start(this)
    MainActivity.start(this)
    finish()
  }

  companion object {
    fun start(context: Context) {
      val intent = Intent(context, WizardActivity::class.java)
      context.startActivity(intent)
    }
  }
}