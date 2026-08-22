package com.repforge.core.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.repforge.core.database.dao.SyncOperationDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Offline-first sync: Room is authoritative while training.
 * Room → Sync Queue (Room) → Cloud (Firestore) via WorkManager.
 * - Never blocks set logging on network
 * - Retries with backoff
 * - Handles conflict via last-write-wins for MVP, CRDT later
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncDao: SyncOperationDao,
    private val engine: SyncEngine,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val pending = syncDao.getPending()
        if (pending.isEmpty()) return Result.success()
        var failed = 0
        for (op in pending) {
            val ok = engine.push(op)
            if (ok) syncDao.markSynced(op.operationId) else failed++
        }
        return if (failed == 0) Result.success() else Result.retry()
    }
}

interface SyncEngine {
    suspend fun push(op: com.repforge.core.database.entity.SyncOperationEntity): Boolean
}

// No-op for V1 without Firebase configured — real impl swaps in FirestoreSyncEngine
class NoOpSyncEngine @javax.inject.Inject constructor() : SyncEngine {
    override suspend fun push(op: com.repforge.core.database.entity.SyncOperationEntity): Boolean = true
}
