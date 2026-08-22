package com.repforge.core.model

enum class SubstitutionReason { EQUIPMENT_UNAVAILABLE, TOO_BUSY, DISLIKE, DISCOMFORT, VARIATION }

data class Substitution(
    val from: Exercise,
    val to: Exercise,
    val reason: SubstitutionReason,
    val similarity: Double, // 0..1
    val whatsSimilar: String,
    val whatsDifferent: String,
)

object ExerciseGraph {
    // Every movement can link to regressions, progressions and substitutes — movement system not 400 isolated entries
    // For pull-up: assisted → band → bodyweight → weighted
    fun rankSubstitutes(
        from: Exercise,
        reason: SubstitutionReason,
        available: Set<Equipment>,
        history: Set<String>, // ids user has done
        gym: GymProfile,
        all: List<Exercise>,
    ): List<Substitution> {
        return all.filter { it.id != from.id && gym.canDo(it) }
            .map { to ->
                var score = 0.0
                if (to.movementPattern == from.movementPattern) score += 0.4
                if (to.primaryMuscles.any { it in from.primaryMuscles }) score += 0.3
                if (to.equipments.any { it in available }) score += 0.15
                if (to.id in history) score += 0.05
                if (to.difficulty == from.difficulty) score += 0.1
                // Penalize if requires gym but hotel profile
                if (to.requiresGym && gym.location == Location.HOME) score -= 0.3
                Substitution(
                    from, to, reason, score.coerceIn(0.0, 1.0),
                    whatsSimilar = "Same ${from.movementPattern.name.lowercase()} • ${from.primaryMuscles.firstOrNull()?.name ?: from.muscleGroup.name}",
                    whatsDifferent = "Equipment ${from.equipment}→${to.equipment} • ${if (to.compound == from.compound) "same compound" else "different"}"
                )
            }
            .sortedByDescending { it.similarity }
            .take(5)
    }
}
