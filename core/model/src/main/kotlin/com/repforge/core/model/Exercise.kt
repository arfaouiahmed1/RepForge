package com.repforge.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class MuscleGroup { CHEST, BACK, LEGS, SHOULDERS, ARMS, CORE, FULL_BODY, GLUTES, BICEPS, TRICEPS, FOREARMS, QUADS, HAMSTRINGS, CALVES, TRAPS, LATS, SERRATUS, FRONT_DELTOID, REAR_DELTOID }

@Serializable
enum class Equipment { BARBELL, DUMBBELL, MACHINE, CABLE, BODYWEIGHT, KETTLEBELL, BAND, SMITH, EZ_BAR, NONE }

@Serializable
enum class Location { HOME, GYM, BOTH }

@Serializable
enum class Environment { GYM, HOME, OUTDOOR, MOBILITY, CARDIO }

@Serializable
enum class MovementPattern { HORIZONTAL_PUSH, VERTICAL_PUSH, HORIZONTAL_PULL, VERTICAL_PULL, SQUAT, HINGE, LUNGE, CARRY, ROTATION, LOCOMOTION, ISOLATION }

@Serializable
enum class Modality { STRENGTH, CARDIO, MOBILITY }

@Serializable
enum class TrackingType { WEIGHT_REPS, REPS_ONLY, TIME, DISTANCE, WEIGHT_DISTANCE, BODYWEIGHT_REPS }

@Serializable
enum class Difficulty { BEGINNER, INTERMEDIATE, ADVANCED }

@Serializable
data class Exercise(
    val id: String,
    val name: String,
    val canonicalName: String = name, // keep name for compat
    val muscleGroup: MuscleGroup,
    val primaryMuscles: List<MuscleGroup> = listOf(muscleGroup),
    val secondaryMuscles: List<MuscleGroup> = emptyList(),
    val equipment: Equipment,
    val equipments: List<Equipment> = listOf(equipment), // new: multi-equipment
    val location: Location = Location.GYM,
    val environments: List<Environment> = listOf(
        when (location) {
            Location.HOME -> Environment.HOME
            Location.GYM -> Environment.GYM
            Location.BOTH -> Environment.GYM
        }
    ),
    val movementPattern: MovementPattern = MovementPattern.ISOLATION,
    val modality: Modality = Modality.STRENGTH,
    val difficulty: Difficulty = Difficulty.INTERMEDIATE,
    val unilateral: Boolean = false,
    val compound: Boolean = true,
    val bodyweight: Boolean = equipment == Equipment.BODYWEIGHT,
    val trackingType: TrackingType = if (equipment == Equipment.BODYWEIGHT) TrackingType.REPS_ONLY else TrackingType.WEIGHT_REPS,
    val description: String? = null,
    val instructions: List<String> = emptyList(),
    val setupCues: List<String> = emptyList(),
    val executionCues: List<String> = emptyList(),
    val commonMistakes: List<String> = emptyList(),
    val tips: List<String> = emptyList(),
    val videoUrl: String? = null,
    val thumbnailUrl: String? = null,
    val thumbnailAsset: String? = null, // 1024x1024 webp rendered in Blender
    val glbAsset: String? = null, // e.g. models/bench_press.glb for 3D
    val animationAsset: String? = glbAsset, // glTF/GLB
    val muscleHeat: Map<String, Float> = emptyMap(), // muscle -> activation 0..1 for 3D tint
    val defaultRestSeconds: Int = 90,
    val alternatives: List<String> = emptyList(), // exercise ids
    val regressions: List<String> = emptyList(),
    val progressions: List<String> = emptyList(),
    val source: String? = null, // e.g. wger
    val sourceId: String? = null,
    val license: String? = null, // CC-BY etc per entry
    val attribution: String? = null,
    val isCustom: Boolean = false,
) {
    val isNoEquipment: Boolean get() = equipment == Equipment.BODYWEIGHT || equipment == Equipment.NONE
    val isHomeFriendly: Boolean get() = location == Location.HOME || location == Location.BOTH || equipment in setOf(Equipment.BODYWEIGHT, Equipment.BAND, Equipment.DUMBBELL, Equipment.KETTLEBELL, Equipment.NONE)
    val requiresGym: Boolean get() = location == Location.GYM && equipment in setOf(Equipment.BARBELL, Equipment.MACHINE, Equipment.CABLE, Equipment.SMITH)
}

@Serializable
data class Routine(
    val id: String,
    val name: String,
    val description: String? = null,
    val dayOfWeek: Int? = null, // 0-6 Mon-Sun
    val estimatedMin: Int = 45,
    val level: Difficulty = Difficulty.INTERMEDIATE,
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
data class RoutineExercise(
    val routineId: String,
    val exerciseId: String,
    val position: Int,
    val targetSets: Int,
    val targetReps: Int,
    val targetRir: Int? = null, // reps in reserve
    val restSeconds: Int = 90,
    val notes: String? = null,
)

@Serializable
enum class SessionState { NOT_STARTED, ACTIVE, RESTING, PAUSED, COMPLETED }

@Serializable
data class TrainingSession(
    val id: String,
    val routineId: String?,
    val routineName: String?,
    val state: SessionState,
    val startedAt: Long,
    val completedAt: Long? = null,
    val totalVolumeKg: Double = 0.0,
    val durationSeconds: Int? = null,
)

@Serializable
data class SetLog(
    val setId: String,
    val sessionId: String,
    val exerciseId: String,
    val exerciseName: String,
    val setIndex: Int, // 1-based within exercise
    val weightKg: Double,
    val targetReps: Int,
    val completedReps: Int,
    val rir: Int? = null, // reps in reserve
    val rpe: Float? = null,
    val restSeconds: Int? = null,
    val durationSeconds: Int? = null,
    val isWarmup: Boolean = false,
    val isFailure: Boolean = false,
    val timestamp: Long,
    val recommendationId: String? = null,
    val recommendationAccepted: Boolean? = null,
)

@Serializable
data class PersonalRecord(
    val exerciseId: String,
    val exerciseName: String,
    val weightKg: Double,
    val reps: Int,
    val estimated1RM: Double,
    val achievedAt: Long,
)

@Serializable
data class BodyMetric(
    val id: String,
    val weightKg: Double?,
    val heightCm: Double? = null,
    val bodyFatPct: Double? = null,
    val muscleMassKg: Double? = null,
    val bmi: Double? = null, // computed: weight / (heightM^2)
    val measuredAt: Long,
    val source: String = "manual", // manual | health_connect
) {
    fun computeBmi(): Double? {
        if (weightKg == null || heightCm == null || heightCm == 0.0) return null
        val h = heightCm / 100.0
        return weightKg / (h * h)
    }
}

@Serializable
data class Recommendation(
    val id: String,
    val exerciseId: String,
    val recommendedLoadKg: Double,
    val targetReps: Int,
    val successProbability: Float, // 0..1
    val explanation: String,
    val createdAt: Long,
)

@Serializable
data class RecommendationOutcome(
    val recommendationId: String,
    val accepted: Boolean,
    val chosenLoadKg: Double,
    val completedReps: Int,
    val rir: Int?,
    val succeeded: Boolean,
)

@Serializable
data class HealthSnapshot(
    val date: String, // yyyy-MM-dd
    val weightKg: Double?,
    val sleepHours: Double?,
    val restingHr: Int?,
    val heartRate: Int?,
)

@Serializable
data class Entitlement(
    val userId: String,
    val productId: String,
    val status: String, // active | expired | revoked
    val expiresAt: Long?,
    val lastVerifiedAt: Long,
)
