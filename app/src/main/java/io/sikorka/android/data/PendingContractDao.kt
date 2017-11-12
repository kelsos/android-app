package io.sikorka.android.data

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import io.reactivex.Flowable


@Dao
abstract class PendingContractDao : BaseDao<PendingContract> {

  @Query("SELECT * FROM pending_contracts")
  abstract fun getAllPendingContracts(): Flowable<List<PendingContract>>
}