package io.sikorka.android.ui

import io.sikorka.android.ui.accounts.AccountAdapterPresenter
import io.sikorka.android.ui.accounts.AccountAdapterPresenterImpl
import io.sikorka.android.ui.accounts.AccountViewModel
import io.sikorka.android.ui.accounts.accountcreation.AccountCreationDialogViewModel
import io.sikorka.android.ui.accounts.accountexport.AccountExportViewModel
import io.sikorka.android.ui.accounts.accountimport.AccountImportViewModel
import io.sikorka.android.ui.contracts.DeployContractViewModel
import io.sikorka.android.ui.contracts.deploydetectorcontract.DeployDetectorViewModel
import io.sikorka.android.ui.contracts.interact.ContractInteractViewModel
import io.sikorka.android.ui.contracts.pending.PendingContractsViewModel
import io.sikorka.android.ui.detector.bluetooth.FindBtDetectorViewModel
import io.sikorka.android.ui.main.MainViewModel
import io.sikorka.android.ui.settings.peermanager.PeerManagerViewModel
import io.sikorka.android.ui.wizard.WizardViewModel
import io.sikorka.android.ui.wizard.slides.accountsetup.AccountSetupViewModel
import io.sikorka.android.ui.wizard.slides.networkselection.NetworkSelectionViewModel
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

val viewModule = module {
  viewModel { create<AccountCreationDialogViewModel>() }
  viewModel { create<AccountImportViewModel>() }
  factory { create<AccountAdapterPresenterImpl>() as AccountAdapterPresenter }
  viewModel { create<AccountViewModel>() }
  viewModel { create<AccountExportViewModel>() }
  viewModel { create<NetworkSelectionViewModel>() }
  viewModel { create<AccountSetupViewModel>() }
  viewModel { create<PendingContractsViewModel>() }
  viewModel { create<DeployContractViewModel>() }
  viewModel { create<DeployDetectorViewModel>() }

  viewModel { create<PeerManagerViewModel>() }
  viewModel { create<WizardViewModel>() }
  viewModel { create<MainViewModel>() }
  viewModel { create<FindBtDetectorViewModel>() }
  viewModel { create<ContractInteractViewModel>() }
}