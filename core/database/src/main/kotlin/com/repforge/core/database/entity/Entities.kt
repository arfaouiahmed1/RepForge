package com.repforge.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val canonicalName: String = name,
    val muscleGroup: String,
    val primaryMuscles: String = muscleGroup, // csv
    val secondaryMuscles: String = "", // csv of MuscleGroup
    val equipment: String,
    val equipments: String = equipment, // csv for multi
    val location: String = "GYM", // HOME, GYM, BOTH
    val environments: String = location, // csv Environment GYM/HOME/OUTDOOR/MOBILITY/CARDIO
    val movementPattern: String = "ISOLATION",
    val modality: String = "STRENGTH",
    val difficulty: String = "INTERMEDIATE",
    val unilateral: Boolean = false,
    val compound: Boolean = true,
    val bodyweight: Boolean = false,
    val trackingType: String = "WEIGHT_REPS", // WEIGHT_REPS/REPS_ONLY/TIME/DISTANCE/WEIGHT_DISTANCE
    val description: String? = null,
    val instructions: String? = null, // json list
    val setupCues: String? = null, // json list
    val executionCues: String? = null, // json list
    val commonMistakes: String? = null, // json list
    val videoUrl: String? = null,
    val thumbnailUrl: String? = null, // rendered 1024x1024 webp
    val thumbnailAsset: String? = null,
    val animationAsset: String? = null, // glTF
    val glbAsset: String? = null,
    val defaultRestSeconds: Int = 90,
    val alternatives: String? = null, // csv ids
    val regressions: String? = null, // csv ids
    val progressions: String? = null, // csv ids
    val source: String? = null,
    val sourceId: String? = null,
    val license: String? = null,
    val attribution: String? = null,
    val isCustom: Boolean = false,
)

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val dayOfWeek: Int?,
    val estimatedMin: Int = 45,
    val level: String = "INTERMEDIATE",
    val createdAt: Long,
    val updatedAt: Long,
    /** Sync bookkeeping (schema v4): bumped on every local mutation; server is merge authority. */
    @ColumnInfo(defaultValue = "0") val revision: Long = 0,
    /** Tombstone (schema v4): non-null = soft-deleted; hard delete only via sync GC. */
    val deletedAt: Long? = null,
)

@Entity(tableName = "routine_exercises", primaryKeys = ["routineId", "exerciseId", "position"])
data class RoutineExerciseEntity(
    val routineId: String,
    val exerciseId: String,
    val position: Int,
    val targetSets: Int,
    val targetReps: Int,
    val targetRir: Int?,
    val restSeconds: Int,
    @ColumnInfo(defaultValue = "0") val createdAt: Long = 0,
    @ColumnInfo(defaultValue = "0") val updatedAt: Long = 0,
    @ColumnInfo(defaultValue = "0") val revision: Long = 0,
    val deletedAt: Long? = null,
)

@Entity(tableName = "training_sessions")
data class TrainingSessionEntity(
    @PrimaryKey val id: String,
    val routineId: String?,
    val routineName: String?,
    val state: String,
    val startedAt: Long,
    val completedAt: Long?,
    @ColumnInfo(defaultValue = "0") val createdAt: Long = startedAt,
    @ColumnInfo(defaultValue = "0") val updatedAt: Long = startedAt,
    @ColumnInfo(defaultValue = "0") val revision: Long = 0,
    val deletedAt: Long? = null,
)

@Entity(tableName = "set_logs")
data class SetLogEntity(
    @PrimaryKey val setId: String,
    val sessionId: String,
    val exerciseId: String,
    val exerciseName: String,
    val setIndex: Int,
    val weightKg: Double,
    val targetReps: Int,
    val completedReps: Int,
    val rir: Int?,
    val rpe: Float?,
    val restSeconds: Int?,
    val durationSeconds: Int?,
    val isWarmup: Boolean,
    val isFailure: Boolean,
    val timestamp: Long,
    val recommendationId: String?,
    val recommendationAccepted: Boolean?,
    @ColumnInfo(defaultValue = "0") val createdAt: Long = timestamp,
    @ColumnInfo(defaultValue = "0") val updatedAt: Long = timestamp,
    @ColumnInfo(defaultValue = "0") val revision: Long = 0,
    val deletedAt: Long? = null,
)

@Entity(tableName = "personal_records", primaryKeys = ["exerciseId"])
data class PersonalRecordEntity(
    val exerciseId: String,
    val exerciseName: String,
    val weightKg: Double,
    val reps: Int,
    val estimated1RM: Double,
    val achievedAt: Long,
)

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val gender: String,
    val birthEpochMs: Long?,
    val heightCm: Double?,
    val weightKg: Double?,
    val goal: String,
    val experience: String,
    val trainingDaysPerWeek: Int,
    val units: String,
    val createdAt: Long,
    val updatedAt: Long,
    @ColumnInfo(defaultValue = "0") val revision: Long = 0,
    val deletedAt: Long? = null,
)

@Entity(tableName = "body_metrics")
data class BodyMetricEntity(
    @PrimaryKey val id: String,
    val weightKg: Double?,
    val heightCm: Double?,
    val bodyFatPct: Double?,
    val muscleMassKg: Double?,
    val bmi: Double?,
    val measuredAt: Long,
    val source: String,
    @ColumnInfo(defaultValue = "0") val createdAt: Long = measuredAt,
    @ColumnInfo(defaultValue = "0") val updatedAt: Long = measuredAt,
    @ColumnInfo(defaultValue = "0") val revision: Long = 0,
    val deletedAt: Long? = null,
)

/**
 * Judgment call (todo 9): achievements are a server-derived unlock catalog — immutable once
 * unlocked, no user-facing delete path, so tombstones/revision are meaningless here. Excluded
 * from the tombstone model for the same reason as ExerciseEntity (catalog versioned separately).
 */
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val tier: String,
    val icon: String,
    val unlockedAt: Long?,
    val progress: Float,
    val targetValue: Double?,
)

/**
 * Outbox row for POST /v1/sync/push (mirrors backend sync_operations wire contract).
 * operationId is the client-generated UUID primary key so replays are idempotent;
 * idempotencyKey is UNIQUE at the schema level as a second guard against double-enqueue.
 */
@Entity(
    tableName = "sync_operations",
    indices = [Index(value = ["idempotencyKey"], unique = true)],
)
data class SyncOperationEntity(
    @PrimaryKey val operationId: String,
    val entityType: String,
    val entityId: String,
    val operation: String, // insert | update | delete
    val payloadJson: String,
    /** Expected server revision; mismatch ⇒ CONFLICT in push results. */
    @ColumnInfo(defaultValue = "0") val baseRevision: Long = 0,
    val idempotencyKey: String,
    val createdAt: Long,
    @ColumnInfo(defaultValue = "0") val synced: Boolean = false,
)
