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
    @Query("SELECT * FROM routines WHERE deletedAt IS NULL ORDER BY dayOfWeek, createdAt")
    fun observeAll(): Flow<List<RoutineEntity>>
    @Query("SELECT * FROM routines WHERE id = :id AND deletedAt IS NULL")
    suspend fun getById(id: String): RoutineEntity?
    @Upsert
    suspend fun upsert(routine: RoutineEntity)
    @Upsert
    suspend fun upsertAll(routines: List<RoutineEntity>)
    /** Soft delete: tombstone + revision bump; row stays for sync pull until server GC. */
    @Query(
        "UPDATE routines SET deletedAt = :now, updatedAt = :now, revision = revision + 1 " +
            "WHERE id = :id AND deletedAt IS NULL"
    )
    suspend fun softDelete(id: String, now: Long)
    /** Sync query: includes tombstoned rows so deletions propagate to the server. */
    @Query("SELECT * FROM routines")
    suspend fun getAllForSync(): List<RoutineEntity>
    /** Transactional merge by id (cloud → room replay); upsert keeps highest supplied fields. */
    @Transaction
    suspend fun mergeById(items: List<RoutineEntity>) {
        items.forEach { upsert(it) }
    }
}

@Dao
interface RoutineExerciseDao {
    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId AND deletedAt IS NULL ORDER BY position")
    fun observeForRoutine(routineId: String): Flow<List<RoutineExerciseEntity>>
    @Upsert
    suspend fun upsertAll(items: List<RoutineExerciseEntity>)
    /** Structural reset used by the seeder when replacing a routine's exercise lines. */
    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    suspend fun clearForRoutine(routineId: String)
    /** User-visible delete of a whole routine's lines: tombstone instead of hard delete. */
    @Query(
        "UPDATE routine_exercises SET deletedAt = :now, updatedAt = :now, revision = revision + 1 " +
            "WHERE routineId = :routineId AND deletedAt IS NULL"
    )
    suspend fun softDeleteForRoutine(routineId: String, now: Long)
    @Query("SELECT * FROM routine_exercises")
    suspend fun getAllForSync(): List<RoutineExerciseEntity>
    @Transaction
    suspend fun mergeById(items: List<RoutineExerciseEntity>) {
        items.forEach { upsertAll(listOf(it)) }
    }
}

@Dao
interface TrainingSessionDao {
    @Query("SELECT * FROM training_sessions WHERE deletedAt IS NULL ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<TrainingSessionEntity>>
    @Query("SELECT * FROM training_sessions WHERE id = :id AND deletedAt IS NULL")
    suspend fun getById(id: String): TrainingSessionEntity?
    @Upsert
    suspend fun upsert(session: TrainingSessionEntity)
    @Query(
        "UPDATE training_sessions SET deletedAt = :now, updatedAt = :now, revision = revision + 1 " +
            "WHERE id = :id AND deletedAt IS NULL"
    )
    suspend fun softDelete(id: String, now: Long)
    @Query("SELECT * FROM training_sessions")
    suspend fun getAllForSync(): List<TrainingSessionEntity>
    @Transaction
    suspend fun mergeById(items: List<TrainingSessionEntity>) {
        items.forEach { upsert(it) }
    }
}

@Dao
interface SetLogDao {
    @Query("SELECT * FROM set_logs WHERE sessionId = :sessionId AND deletedAt IS NULL ORDER BY timestamp")
    fun observeForSession(sessionId: String): Flow<List<SetLogEntity>>
    @Query("SELECT * FROM set_logs WHERE exerciseId = :exerciseId AND deletedAt IS NULL ORDER BY timestamp DESC LIMIT :limit")
    fun observeForExercise(exerciseId: String, limit: Int = 50): Flow<List<SetLogEntity>>
    @Query("SELECT * FROM set_logs WHERE deletedAt IS NULL ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int = 100): Flow<List<SetLogEntity>>
    @Upsert
    suspend fun upsert(set: SetLogEntity)
    @Query(
        "UPDATE set_logs SET deletedAt = :now, updatedAt = :now, revision = revision + 1 " +
            "WHERE setId = :setId AND deletedAt IS NULL"
    )
    suspend fun softDelete(setId: String, now: Long)
    @Query("SELECT * FROM set_logs")
    suspend fun getAllForSync(): List<SetLogEntity>
    @Transaction
    suspend fun mergeById(items: List<SetLogEntity>) {
        items.forEach { upsert(it) }
    }
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE deletedAt IS NULL LIMIT 1")
    fun observe(): Flow<UserProfileEntity?>
    @Query("SELECT * FROM user_profiles WHERE deletedAt IS NULL LIMIT 1")
    suspend fun get(): UserProfileEntity?
    @Upsert
    suspend fun upsert(profile: UserProfileEntity)
    @Query(
        "UPDATE user_profiles SET deletedAt = :now, updatedAt = :now, revision = revision + 1 " +
            "WHERE id = :id AND deletedAt IS NULL"
    )
    suspend fun softDelete(id: String, now: Long)
    @Query("SELECT * FROM user_profiles")
    suspend fun getAllForSync(): List<UserProfileEntity>
    @Transaction
    suspend fun mergeById(items: List<UserProfileEntity>) {
        items.forEach { upsert(it) }
    }
}

@Dao
interface BodyMetricDao {
    @Query("SELECT * FROM body_metrics WHERE deletedAt IS NULL ORDER BY measuredAt DESC")
    fun observeAll(): Flow<List<BodyMetricEntity>>
    @Query("SELECT * FROM body_metrics WHERE deletedAt IS NULL ORDER BY measuredAt DESC LIMIT 1")
    fun observeLatest(): Flow<BodyMetricEntity?>
    @Upsert
    suspend fun upsert(metric: BodyMetricEntity)
    @Query(
        "UPDATE body_metrics SET deletedAt = :now, updatedAt = :now, revision = revision + 1 " +
            "WHERE id = :id AND deletedAt IS NULL"
    )
    suspend fun softDelete(id: String, now: Long)
    @Query("SELECT * FROM body_metrics")
    suspend fun getAllForSync(): List<BodyMetricEntity>
    @Transaction
    suspend fun mergeById(items: List<BodyMetricEntity>) {
        items.forEach { upsert(it) }
    }
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
    @Query("SELECT * FROM sync_operations ORDER BY createdAt")
    suspend fun getAll(): List<SyncOperationEntity>
    /**
     * Idempotent enqueue: a replayed operationId is IGNOREd by primary-key conflict and
     * returns -1 (Room convention for ignored inserts). Unique idempotencyKey index backs
     * this at the schema level.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(op: SyncOperationEntity): Long
    @Query("SELECT EXISTS(SELECT 1 FROM sync_operations WHERE operationId = :operationId)")
    suspend fun existsByOperationId(operationId: String): Boolean
    @Query("UPDATE sync_operations SET synced = 1 WHERE operationId = :operationId")
    suspend fun markSynced(operationId: String)
    /** GC for the outbox: drop acknowledged rows older than the cutoff. */
    @Query("DELETE FROM sync_operations WHERE synced = 1 AND createdAt < :beforeEpochMs")
    suspend fun pruneSyncedBefore(beforeEpochMs: Long)
}
