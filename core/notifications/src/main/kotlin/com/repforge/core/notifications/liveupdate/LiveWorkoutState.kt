package com.repforge.core.notifications.liveupdate

/**
 * State driving the Live Update — single source for notification + chip + progress.
 * Maps actual workout to ProgressStyle: each segment = exercise, width = working sets, tracker = completed-set position.
 * Also supports outdoor: RUN 4.72km / HIKE 7.3km via same subsystem (strength vs cardio).
 */
enum class WorkoutKind { STRENGTH, RUN, HIKE, CYCLE, WALK, INTERVAL }

data class LiveWorkoutState(
    val kind: WorkoutKind = WorkoutKind.STRENGTH,
    val routineName: String = "PUSH DAY",
    val exerciseName: String = "Bench Press",
    val exerciseIndex: Int = 3, // 1-based
    val totalExercises: Int = 7,
    val setIndex: Int = 3, // 1-based within exercise
    val setsPerExercise: Int = 4,
    // Per-exercise working sets for segment widths — e.g. [4,3,3,4,3] for PUSH
    val setsPerExerciseList: List<Int> = emptyList(),
    val totalSets: Int = 28, // sum(setsPerExerciseList) or 7*4
    val completedSets: Int = 9,
    val currentLoadKg: Double = 82.5,
    val currentReps: Int = 8,
    val restRemainingSec: Int? = null, // null = lifting, value = resting
    val restTotalSec: Int = 90,
    val nextExercise: String? = "Incline DB Press",
    val nextLoad: String? = "32 kg × 10",
    val successProbability: Float? = 0.78f,
    val elapsedSec: Long = 742, // 12:22
    // Cardio fields — reused for RUN/HIKE/CYCLE
    val distanceKm: Double? = null, // 4.72
    val paceMinPerKm: String? = null, // "5:09"
    val goalKm: Double? = null, // 6.0
    val elevationM: Int? = null, // 412
) {
    val isResting: Boolean get() = restRemainingSec != null
    val overallProgress: Int get() = if (totalSets == 0) 0 else (completedSets * 100 / totalSets).coerceIn(0, 100)
    val exerciseProgressText: String get() = "$exerciseIndex / $totalExercises"
    val setProgressText: String get() = "$setIndex / $setsPerExercise"
    // Widths for ProgressStyle segments — if list empty fallback to equal
    fun segmentWidths(): List<Int> {
        if (setsPerExerciseList.isEmpty() || setsPerExerciseList.size != totalExercises) return List(totalExercises) { 100 / totalExercises.coerceAtLeast(1) }
        val total = setsPerExerciseList.sum().coerceAtLeast(1)
        // Distribute 100 across segments proportionally to sets, ensuring sum == 100 via remainder
        val base = setsPerExerciseList.map { it * 100 / total }
        val remainder = 100 - base.sum()
        return base.mapIndexed { i, v -> if (i == 0) v + remainder else v }
    }
    // Cardio progress 0..100
    val cardioProgress: Int? get() {
        if (distanceKm != null && goalKm != null && goalKm != 0.0) return ((distanceKm / goalKm) * 100).toInt().coerceIn(0, 100)
        return null
    }
}
