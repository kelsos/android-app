package io.sikorka.android.core.monitor

import io.reactivex.disposables.Disposable
import io.sikorka.android.core.ethereumclient.LightClientProvider
import io.sikorka.android.core.model.TransactionReceipt
import io.sikorka.android.data.contracts.pending.PendingContractDao
import io.sikorka.android.data.observeNonNull
import io.sikorka.android.data.syncstatus.SyncStatus
import io.sikorka.android.data.syncstatus.SyncStatusProvider
import io.sikorka.android.events.Event
import io.sikorka.android.events.EventLiveDataProvider
import io.sikorka.android.utils.schedulers.AppDispatchers
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.withContext

class PendingContractMonitor(
  syncStatusProvider: SyncStatusProvider,
  private val lightClientProvider: LightClientProvider,
  private val pendingContractDao: PendingContractDao,
  private val dispatchers: AppDispatchers,
  private val bus: EventLiveDataProvider
) : LifecycleMonitor() {
  private var disposable: Disposable? = null
  private var statusUpdateListener: statusUpdateListener? = null
  private var deferred: Deferred<List<Unit>>? = null

  init {
    syncStatusProvider.observeNonNull(this) { status: SyncStatus ->
      val deferredIsActive = deferred?.isActive ?: false
      if (!status.syncing || !lightClientProvider.initialized || deferredIsActive) {
        return@observeNonNull
      }

      val lightClient = lightClientProvider.get()

      deferred = async(dispatchers.io) {
        withContext(dispatchers.db) {
          pendingContractDao.getAllPendingContractsList()
        }.map { pendingTransaction ->
          lightClient.getTransactionReceipt(pendingTransaction.transactionHash)
            .map { it.withContractAddress(pendingTransaction.contractAddress) }
            .toOption().fold({ Unit }) { receipt: TransactionReceipt ->
              pendingContractDao.deleteByContractAddress(receipt.contractAddress())
              statusUpdateListener?.invoke(receipt)
              bus.post(Event(ContractStatus(receipt.contractAddress(), receipt.txHash, receipt.successful)))
            }
        }
      }


    }
  }

  override fun stop() {
    super.stop()
    disposable?.dispose()
  }

  fun setStatusUpdateListener(
    statusUpdateListener: statusUpdateListener?
  ) {
    this.statusUpdateListener = statusUpdateListener
  }
}