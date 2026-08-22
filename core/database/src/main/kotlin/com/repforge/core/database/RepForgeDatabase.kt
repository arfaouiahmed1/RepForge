package com.repforge.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.repforge.core.database.dao.*
import com.repforge.core.database.entity.*

@Database(
    entities = [
        ExerciseEntity::class,
        RoutineEntity::class,
        RoutineExerciseEntity::class,
        TrainingSessionEntity::class,
        SetLogEntity::class,
        PersonalRecordEntity::class,
        UserProfileEntity::class,
        BodyMetricEntity::class,
        AchievementEntity::class,
        SyncOperationEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class RepForgeDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun routineDao(): RoutineDao
    abstract fun routineExerciseDao(): RoutineExerciseDao
    abstract fun trainingSessionDao(): TrainingSessionDao
    abstract fun setLogDao(): SetLogDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun bodyMetricDao(): BodyMetricDao
    abstract fun achievementDao(): AchievementDao
    abstract fun syncOperationDao(): SyncOperationDao
}
