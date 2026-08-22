package com.repforge.core.data.repository

import com.repforge.core.database.dao.SetLogDao
import com.repforge.core.database.dao.TrainingSessionDao
import com.repforge.core.database.entity.SetLogEntity
import com.repforge.core.database.entity.TrainingSessionEntity
import com.repforge.core.model.SetLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import java.util.UUID

/**
 * Offline-first: Room is source of truth. Sync queue writes to Firestore via WorkManager.
 * Never blocks set logging on network.
 */
@Singleton
class WorkoutRepository @Inject constructor(
    private val sessionDao: TrainingSessionDao,
    private val setLogDao: SetLogDao,
) {
    fun observeSessions() = sessionDao.observeAll()
    fun observeSets(sessionId: String): Flow<List<SetLog>> =
        setLogDao.observeForSession(sessionId).map { list -> list.map { it.toModel() } }

    suspend fun startSession(routineId: String?, routineName: String?): String {
        val id = UUID.randomUUID().toString()
        sessionDao.upsert(
            TrainingSessionEntity(
                id = id,
                routineId = routineId,
                routineName = routineName,
                state = "ACTIVE",
                startedAt = System.currentTimeMillis(),
                completedAt = null
            )
        )
        return id
    }

    suspend fun logSet(set: SetLog) {
        setLogDao.upsert(set.toEntity())
        // enqueue sync — WorkManager will handle offline queue
    }

    suspend fun finishSession(sessionId: String) {
        val s = sessionDao.getById(sessionId) ?: return
        sessionDao.upsert(s.copy(state = "COMPLETED", completedAt = System.currentTimeMillis()))
    }
}

private fun SetLog.toEntity() = SetLogEntity(
    setId = setId, sessionId = sessionId, exerciseId = exerciseId, exerciseName = exerciseName,
    setIndex = setIndex, weightKg = weightKg, targetReps = targetReps, completedReps = completedReps,
    rir = rir, rpe = rpe, restSeconds = restSeconds, durationSeconds = durationSeconds,
    isWarmup = isWarmup, isFailure = isFailure, timestamp = timestamp,
    recommendationId = recommendationId, recommendationAccepted = recommendationAccepted
)
private fun SetLogEntity.toModel() = SetLog(
    setId = setId, sessionId = sessionId, exerciseId = exerciseId, exerciseName = exerciseName,
    setIndex = setIndex, weightKg = weightKg, targetReps = targetReps, completedReps = completedReps,
    rir = rir, rpe = rpe, restSeconds = restSeconds, durationSeconds = durationSeconds,
    isWarmup = isWarmup, isFailure = isFailure, timestamp = timestamp,
    recommendationId = recommendationId, recommendationAccepted = recommendationAccepted
)
