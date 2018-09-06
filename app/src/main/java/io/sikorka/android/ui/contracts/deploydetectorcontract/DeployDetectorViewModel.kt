package io.sikorka.android.ui.contracts.deploydetectorcontract

import androidx.lifecycle.ViewModel
import io.sikorka.android.core.GethNode
import io.sikorka.android.core.contracts.model.ContractGas
import io.sikorka.android.core.contracts.model.DetectorContractData
import io.sikorka.android.data.contracts.ContractRepository
import io.sikorka.android.settings.AppPreferences
import io.sikorka.android.ui.contracts.DeployContractCodes
import io.sikorka.android.utils.schedulers.AppDispatchers
import io.sikorka.android.utils.schedulers.AppSchedulers
import kotlinx.coroutines.experimental.launch
import timber.log.Timber

class DeployDetectorViewModel(
  private val gethNode: GethNode,
  private val contractRepository: ContractRepository,
  private val dispatchers: AppDispatchers,
  private val appPreferences: AppPreferences
) : ViewModel() {
  fun prepareGasSelection() {

    gethNode.suggestedGasPrice()
  }

  fun deployContract(passphrase: String, data: DetectorContractData) {
    launch(dispatchers.io) {
      contractRepository.deployContract(passphrase, data)
    }

  }

  fun prepareDeployWithDefaults() {
    val gasLimit = appPreferences.preferredGasLimit()
    val gasPrice = appPreferences.preferredGasPrice()

    if (gasLimit < 0 || gasPrice < 0) {
     // attachedView().showError(DeployContractCodes.NO_GAS_PREFERENCES)
    } else {
      //attachedView().requestDeployAuthorization(ContractGas(gasPrice, gasLimit))
    }
  }
}