package com.repforge.core.data.repository

import androidx.room.withTransaction
import com.repforge.core.database.RepForgeDatabase
import com.repforge.core.database.dao.SetLogDao
import com.repforge.core.database.dao.SyncOperationDao
import com.repforge.core.database.dao.TrainingSessionDao
import com.repforge.core.database.entity.SetLogEntity
import com.repforge.core.database.entity.SyncOperationEntity
import com.repforge.core.database.entity.TrainingSessionEntity
import com.repforge.core.model.SetLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton
import java.util.UUID

/**
 * Offline-first: Room is the ONLY source of truth. Every mutation persists locally and
 * enqueues an idempotent SyncOperation inside one Room transaction; WorkManager flushes
 * the outbox to the backend later. UI never talks to the network directly.
 */
@Singleton
class WorkoutRepository @Inject constructor(
    private val sessionDao: TrainingSessionDao,
    private val setLogDao: SetLogDao,
    private val db: RepForgeDatabase,
    private val syncDao: SyncOperationDao,
) {
    fun observeSessions() = sessionDao.observeAll()
    fun observeSets(sessionId: String): Flow<List<SetLog>> =
        setLogDao.observeForSession(sessionId).map { list -> list.map { it.toModel() } }

    suspend fun startSession(routineId: String?, routineName: String?): String {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        sessionDao.upsert(
            TrainingSessionEntity(
                id = id,
                routineId = routineId,
                routineName = routineName,
                state = "ACTIVE",
                startedAt = now,
                completedAt = null
            )
        )
        return id
    }

    /**
     * Persist one completed set AND enqueue its outbox operation atomically.
     *
     * @param operationId optional caller-supplied id so a retry of a failed flush reuses
     *   the SAME operation (SyncOperationDao.insert is IGNORE-on-conflict by primary key),
     *   keeping replay idempotent end-to-end.
     */
    suspend fun completeSet(set: SetLog, operationId: String = UUID.randomUUID().toString()) {
        val now = System.currentTimeMillis()
        val payload = payloadJson.encodeToString(SetLog.serializer(), set)
        db.withTransaction {
            setLogDao.upsert(set.toEntity(now))
            syncDao.insert(
                SyncOperationEntity(
                    operationId = operationId,
                    entityType = "set_log",
                    entityId = set.setId,
                    operation = "upsert",
                    payloadJson = payload,
                    baseRevision = 0L,
                    idempotencyKey = "set_log:$operationId",
                    createdAt = now,
                )
            )
        }
    }

    /** Backward-compatible alias; new code should call [completeSet]. */
    suspend fun logSet(set: SetLog) = completeSet(set)

    suspend fun finishSession(sessionId: String) {
        val s = sessionDao.getById(sessionId) ?: return
        val now = System.currentTimeMillis()
        sessionDao.upsert(s.copy(state = "COMPLETED", completedAt = now))
    }
}

private val payloadJson = Json { ignoreUnknownKeys = true }

private fun SetLog.toEntity(now: Long = System.currentTimeMillis()): SetLogEntity = SetLogEntity(
    setId = setId, sessionId = sessionId, exerciseId = exerciseId, exerciseName = exerciseName,
    setIndex = setIndex, weightKg = weightKg, targetReps = targetReps, completedReps = completedReps,
    rir = rir, rpe = rpe, restSeconds = restSeconds, durationSeconds = durationSeconds,
    isWarmup = isWarmup, isFailure = isFailure, timestamp = timestamp,
    recommendationId = recommendationId, recommendationAccepted = recommendationAccepted,
    updatedAt = now
)
private fun SetLogEntity.toModel() = SetLog(
    setId = setId, sessionId = sessionId, exerciseId = exerciseId, exerciseName = exerciseName,
    setIndex = setIndex, weightKg = weightKg, targetReps = targetReps, completedReps = completedReps,
    rir = rir, rpe = rpe, restSeconds = restSeconds, durationSeconds = durationSeconds,
    isWarmup = isWarmup, isFailure = isFailure, timestamp = timestamp,
    recommendationId = recommendationId, recommendationAccepted = recommendationAccepted
)
