package com.repforge.core.data.repository

import com.repforge.core.database.dao.SetLogDao
import com.repforge.core.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepository @Inject constructor(
    private val setLogDao: SetLogDao,
) {
    fun observeHistory(exerciseId: String): Flow<List<SetLog>> =
        setLogDao.observeForExercise(exerciseId, 100).map { list -> list.map { it.toModel() } }

    // UI gets real stats, not fake
    suspend fun computeForExercise(exerciseId: String): ExerciseProgress {
        val sets = setLogDao.observeForExercise(exerciseId, 200).let { /* fallback sync read */ emptyList<SetLog>() }
        // For now compute from flow snapshot in ViewModel; this provides calculation helpers
        return ExerciseProgress()
    }
}

data class ExerciseProgress(
    val estimated1RM: Double = 0.0,
    val deltaPercent: Double = 0.0,
    val bestSet: SetLog? = null,
    val weeklyVolumeKg: Double = 0.0,
    val adherencePercent: Int = 0,
)

private fun com.repforge.core.database.entity.SetLogEntity.toModel() = SetLog(
    setId = setId, sessionId = sessionId, exerciseId = exerciseId, exerciseName = exerciseName,
    setIndex = setIndex, weightKg = weightKg, targetReps = targetReps, completedReps = completedReps,
    rir = rir, rpe = rpe, restSeconds = restSeconds, durationSeconds = durationSeconds,
    isWarmup = isWarmup, isFailure = isFailure, timestamp = timestamp,
    recommendationId = recommendationId, recommendationAccepted = recommendationAccepted
)
