package io.sikorka.android.ui.contracts

import androidx.lifecycle.ViewModel
import io.sikorka.android.core.GethNode
import io.sikorka.android.core.contracts.model.ContractData
import io.sikorka.android.core.contracts.model.ContractGas
import io.sikorka.android.data.contracts.ContractRepository
import io.sikorka.android.settings.AppPreferences
import io.sikorka.android.utils.schedulers.AppDispatchers
import io.sikorka.android.utils.schedulers.AppSchedulers
import timber.log.Timber

class DeployContractViewModel(
  private val gethNode: GethNode,
  private val contractRepository: ContractRepository,
  private val dispatchers: AppDispatchers,
  private val appPreferences: AppPreferences
) : ViewModel() {

  fun load() {
    gethNode.suggestedGasPrice()
  }

  fun deployContract(passphrase: String, contractInfo: ContractData) {
    //contractRepository.deployContract(passphrase, contractInfo)
  }

  fun prepareGasSelection() {
    gethNode.suggestedGasPrice()

  }

  fun prepareDeployWithDefaults() {
    val gasLimit = appPreferences.preferredGasLimit()
    val gasPrice = appPreferences.preferredGasPrice()

    if (gasLimit < 0 || gasPrice < 0) {
      //attachedView().showError(DeployContractCodes.NO_GAS_PREFERENCES)
    } else {
     // attachedView().requestDeployAuthorization(ContractGas(gasPrice, gasLimit))
    }
  }
}