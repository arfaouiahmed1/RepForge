package com.repforge.core.model

import kotlin.math.roundToInt

data class MovementPassport(
    val exercise: Exercise,
    val firstLogged: Long?,
    val sessions: Int,
    val workingSets: Int,
    val bestSet: SetLog?,
    val estimated1RM: Double?,
    val totalVolumeKg: Double,
    val milestones: List<String>, // First 100kg, 1.5× BW, 100 sets
) {
    companion object {
        fun from(exercise: Exercise, sets: List<SetLog>, profile: UserProfile?): MovementPassport {
            val sorted = sets.sortedBy { it.timestamp }
            val best = sets.maxByOrNull { estimated1RM(it.weightKg, it.completedReps) }
            val vol = sets.sumOf { it.weightKg * it.completedReps }
            val bw = profile?.weightKg ?: 80.0
            val milestones = mutableListOf<String>()
            if (sets.isNotEmpty()) milestones.add("First logged ${java.text.SimpleDateFormat("MMM yyyy").format(java.util.Date(sorted.first().timestamp))}")
            if ((best?.let { estimated1RM(it.weightKg, it.completedReps) } ?: 0.0) >= 100) milestones.add("First 100 kg")
            if ((best?.weightKg ?: 0.0) >= bw * 1.5) milestones.add("1.5× bodyweight")
            if (sets.size >= 100) milestones.add("100 working sets")
            return MovementPassport(exercise, sorted.firstOrNull()?.timestamp, sorted.map { it.sessionId }.distinct().size, sets.size, best, best?.let { estimated1RM(it.weightKg, it.completedReps) }, vol, milestones)
        }
    }
}
