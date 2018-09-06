package io.sikorka.android.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import io.sikorka.android.core.accounts.AccountModel
import io.sikorka.android.core.accounts.AccountRepository
import io.sikorka.android.data.contracts.ContractRepository
import io.sikorka.android.data.contracts.deployed.DeployedSikorkaContract
import io.sikorka.android.data.location.UserLocation
import io.sikorka.android.data.location.UserLocationProvider
import io.sikorka.android.data.syncstatus.SyncStatus
import io.sikorka.android.data.syncstatus.SyncStatusProvider
import io.sikorka.android.events.Event
import io.sikorka.android.events.EventLiveDataProvider
import io.sikorka.android.settings.AppPreferences
import io.sikorka.android.utils.schedulers.AppSchedulers
import timber.log.Timber

class MainViewModel(
  private val accountRepository: AccountRepository,
  private val contractRepository: ContractRepository,
  private val appSchedulers: AppSchedulers,
  private val locationProvider: UserLocationProvider,
  private val appPreferences: AppPreferences,
  private val syncStatusProvider: SyncStatusProvider,
  private val bus: EventLiveDataProvider
) : ViewModel() {

  fun defaultAccountBalance(): LiveData<AccountModel> {
    return Transformations.map(accountRepository.observeDefaultAccountBalance()) { input ->
      return@map AccountModel(
        input.addressHex,
        input.balance,
        appPreferences.preferredBalancePrecision()
      )
    }
  }

  fun deployedContracts(): LiveData<List<DeployedSikorkaContract>> {
    return contractRepository.getDeployedContracts()
  }

  fun syncStatus(): LiveData<SyncStatus> {
    return syncStatusProvider
  }

  fun events(): LiveData<Event<Any>> {
    return bus.events()
  }

  fun userLocation(userLocation: UserLocation) {
    locationProvider.value = userLocation
  }

  fun load() {
//    addDisposable(accountRepository.selectedAccount()
//      .subscribeOn(appSchedulers.io)
//      .observeOn(appSchedulers.main)
//      .subscribe({
//        attachedView().updateAccountInfo(it, appPreferences.preferredBalancePrecision())
//        Timber.v(it.toString())
//      }) {
//        Timber.v(it)
//      }
//    )
  }
}