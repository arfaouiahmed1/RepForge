package com.repforge.core.database.seed

import com.repforge.core.database.dao.*
import com.repforge.core.database.entity.BodyMetricEntity
import com.repforge.core.database.entity.SetLogEntity
import com.repforge.core.database.entity.TrainingSessionEntity
import com.repforge.core.database.entity.UserProfileEntity
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class DatabaseSeeder @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val routineDao: RoutineDao,
    private val routineExerciseDao: RoutineExerciseDao,
    private val userProfileDao: UserProfileDao,
    private val bodyMetricDao: BodyMetricDao,
    private val setLogDao: SetLogDao,
    private val sessionDao: TrainingSessionDao,
) {
    suspend fun seedIfNeeded() {
        if (exerciseDao.count() == 0) {
            exerciseDao.upsertAll(ExerciseSeed.all)
            routineDao.upsertAll(RoutineSeed.routines)
            routineExercisesSeed()
        } else if (exerciseDao.count() < 100) {
            exerciseDao.upsertAll(ExerciseSeed.extended)
        }
        // Seed profile with height 178cm + weight 82kg for realistic BMI + demo sessions
        if (userProfileDao.get() == null) {
            val now = System.currentTimeMillis()
            userProfileDao.upsert(
                UserProfileEntity("user1", "Ahmed", "MALE", null, 178.0, 82.0, "STRENGTH", "INTERMEDIATE", 4, "METRIC", now, now)
            )
            bodyMetricDao.upsert(
                BodyMetricEntity(UUID.randomUUID().toString(), 82.0, 178.0, 15.0, 64.0, 82.0 / ((1.78)*(1.78)), now - 2*24*3600*1000, "manual")
            )
            bodyMetricDao.upsert(
                BodyMetricEntity(UUID.randomUUID().toString(), 82.5, 178.0, 14.8, 64.5, 82.5 / ((1.78)*(1.78)), now, "manual")
            )
        }
        // Seed 3 demo sessions for Progress sparkline + Ghost comparison
        try {
            val sessions = sessionDao.observeAll().first()
            if (sessions.isEmpty()) {
                val recent = setLogDao.observeRecent(1).first()
                if (recent.isEmpty()) {
                    val now = System.currentTimeMillis()
                    repeat(3) { week ->
                        val sid = UUID.randomUUID().toString()
                        sessionDao.upsert(TrainingSessionEntity(sid, "ppl_push", "PPL — Push", "COMPLETED", now - (14 - week*7)*24*3600*1000L, now - (14 - week*7)*24*3600*1000L + 3600*1000))
                        val load = 77.5 + week*2.5
                        setLogDao.upsert(SetLogEntity(UUID.randomUUID().toString(), sid, "bench_bb", "Bench Press", 1, load, 8, 8, rir = 2 - week.coerceAtMost(1), rpe = null, restSeconds = 90, durationSeconds = null, isWarmup = false, isFailure = false, timestamp = now - (14 - week*7)*24*3600*1000L, recommendationId = null, recommendationAccepted = null))
                        setLogDao.upsert(SetLogEntity(UUID.randomUUID().toString(), sid, "bench_bb", "Bench Press", 2, load, 8, 7, rir = 1, rpe = null, restSeconds = 90, durationSeconds = null, isWarmup = false, isFailure = false, timestamp = now - (14 - week*7)*24*3600*1000L + 60000, recommendationId = null, recommendationAccepted = null))
                    }
                }
            }
        } catch (_: Exception) {}
    }

    private suspend fun routineExercisesSeed() {
        RoutineSeed.routineExercises.groupBy { it.routineId }.forEach { (routineId, list) ->
            routineExerciseDao.clearForRoutine(routineId)
            routineExerciseDao.upsertAll(list)
        }
    }
}


