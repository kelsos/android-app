package io.sikorka.android.ui.wizard.slides.networkselection

import androidx.lifecycle.ViewModel
import io.sikorka.android.core.configuration.Network
import io.sikorka.android.settings.AppPreferences

class NetworkSelectionViewModel(
  private val appPreferences: AppPreferences
) : ViewModel() {

  @Network.Selection
  fun updateSelected(): Int {
    val selectedNetwork = appPreferences.selectedNetwork()
    appPreferences.selectNetwork(selectedNetwork)
    return selectedNetwork
  }

  @Network.Selection
  fun selectNetwork(network: Int): Int {
    appPreferences.selectNetwork(network)
    return network
  }
}