package com.repforge.core.model

import kotlin.math.roundToInt

data class SessionRecap(
    val durationMin: Int,
    val totalVolumeKg: Double,
    val exercisesDone: Int,
    val prs: List<PersonalRecord>,
    val strongestSet: SetLog?,
    val consistencyPct: Int,
    val volumeDeltaPct: Double, // vs last same routine
    val muscleDistribution: Map<MuscleGroup, Int>, // % per group
    val achievements: List<Achievement>,
) {
    fun shareText(): String = buildString {
        appendLine("RepForge — $exercisesDone exercises • ${durationMin}min • ${totalVolumeKg.roundToInt()}kg vol")
        if (prs.isNotEmpty()) appendLine("PRs: ${prs.joinToString { "${it.exerciseName} ${it.weightKg}×${it.reps}" }}")
        append("Strongest: ${strongestSet?.let { "${it.exerciseName} ${it.weightKg}×${it.completedReps}" } ?: "—"}")
    }
}
