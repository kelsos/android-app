package io.sikorka.android.core.monitor

import arrow.core.Try
import io.sikorka.android.core.ethereumclient.LightClientProvider
import io.sikorka.android.core.model.TransactionReceipt
import io.sikorka.android.data.observeNonNull
import io.sikorka.android.data.syncstatus.SyncStatusProvider
import io.sikorka.android.data.transactions.PendingTransactionDao
import io.sikorka.android.utils.schedulers.AppDispatchers
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.withContext
import timber.log.Timber

class PendingTransactionMonitor(
  private val syncStatusProvider: SyncStatusProvider,
  private val pendingTransactionDao: PendingTransactionDao,
  private val dispatchers: AppDispatchers,
  private val lightClientProvider: LightClientProvider
) : LifecycleMonitor() {

  private var deferred: Deferred<Unit>? = null

  override fun start() {
    super.start()
    syncStatusProvider.observeNonNull(this) { status ->
      val deferredIsActive = deferred?.isActive ?: false
      if (!status.syncing || !lightClientProvider.initialized || deferredIsActive) {
        return@observeNonNull
      }

      deferred = checkPendingTransactions()
    }
  }

  private fun checkPendingTransactions(): Deferred<Unit> {
    return async(dispatchers.io) {
      Try {
        val lightClient = lightClientProvider.get()
        withContext(dispatchers.db) {
          pendingTransactionDao.pendingTransaction()
        }.map {
          lightClient.getTransactionReceipt(it.txHash)
        }.flatMap { it.toOption().toList() }
      }.toEither().fold({ emptyList<TransactionReceipt>() }, { receipts ->
        return@fold receipts
      }).forEach {
        Timber.v("receipt ${it.successful} - ${it.txHash}")
      }
    }
  }
}