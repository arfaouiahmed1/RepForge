package com.repforge.core.database.dao

import androidx.room.*
import com.repforge.core.database.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY name")
    fun observeAll(): Flow<List<ExerciseEntity>>
    @Query("SELECT * FROM exercises WHERE muscleGroup = :group ORDER BY name")
    fun observeByMuscle(group: String): Flow<List<ExerciseEntity>>
    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: String): ExerciseEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ExerciseEntity>)
    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun count(): Int
}

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines ORDER BY dayOfWeek, createdAt")
    fun observeAll(): Flow<List<RoutineEntity>>
    @Query("SELECT * FROM routines WHERE id = :id")
    suspend fun getById(id: String): RoutineEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(routine: RoutineEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(routines: List<RoutineEntity>)
    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface RoutineExerciseDao {
    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY position")
    fun observeForRoutine(routineId: String): Flow<List<RoutineExerciseEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<RoutineExerciseEntity>)
    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    suspend fun clearForRoutine(routineId: String)
}

@Dao
interface TrainingSessionDao {
    @Query("SELECT * FROM training_sessions ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<TrainingSessionEntity>>
    @Query("SELECT * FROM training_sessions WHERE id = :id")
    suspend fun getById(id: String): TrainingSessionEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: TrainingSessionEntity)
}

@Dao
interface SetLogDao {
    @Query("SELECT * FROM set_logs WHERE sessionId = :sessionId ORDER BY timestamp")
    fun observeForSession(sessionId: String): Flow<List<SetLogEntity>>
    @Query("SELECT * FROM set_logs WHERE exerciseId = :exerciseId ORDER BY timestamp DESC LIMIT :limit")
    fun observeForExercise(exerciseId: String, limit: Int = 50): Flow<List<SetLogEntity>>
    @Query("SELECT * FROM set_logs ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int = 100): Flow<List<SetLogEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(set: SetLogEntity)
    @Query("DELETE FROM set_logs WHERE setId = :setId")
    suspend fun delete(setId: String)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles LIMIT 1")
    fun observe(): Flow<UserProfileEntity?>
    @Query("SELECT * FROM user_profiles LIMIT 1")
    suspend fun get(): UserProfileEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: UserProfileEntity)
}

@Dao
interface BodyMetricDao {
    @Query("SELECT * FROM body_metrics ORDER BY measuredAt DESC")
    fun observeAll(): Flow<List<BodyMetricEntity>>
    @Query("SELECT * FROM body_metrics ORDER BY measuredAt DESC LIMIT 1")
    fun observeLatest(): Flow<BodyMetricEntity?>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(metric: BodyMetricEntity)
    @Query("DELETE FROM body_metrics WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY unlockedAt DESC")
    fun observeAll(): Flow<List<AchievementEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<AchievementEntity>)
    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getById(id: String): AchievementEntity?
}

@Dao
interface SyncOperationDao {
    @Query("SELECT * FROM sync_operations WHERE synced = 0 ORDER BY createdAt")
    suspend fun getPending(): List<SyncOperationEntity>
    @Insert
    suspend fun insert(op: SyncOperationEntity)
    @Query("UPDATE sync_operations SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: Int)
}
