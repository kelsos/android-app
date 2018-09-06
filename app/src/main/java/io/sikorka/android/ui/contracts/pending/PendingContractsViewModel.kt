package io.sikorka.android.ui.contracts.pending

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.sikorka.android.data.contracts.pending.PendingContract
import io.sikorka.android.data.contracts.pending.PendingContractDao

class PendingContractsViewModel(
  private val pendingContractDao: PendingContractDao
) : ViewModel() {

  fun pendingContracts(): LiveData<List<PendingContract>> {
    return pendingContractDao.getAllPendingContracts()
  }
}