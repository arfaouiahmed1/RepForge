package com.repforge.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class AchievementTier { BRONZE, SILVER, GOLD, PLATINUM, LEGEND }

@Serializable
enum class AchievementCategory { STRENGTH, VOLUME, CONSISTENCY, MILESTONE, BODY }

@Serializable
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val category: AchievementCategory,
    val tier: AchievementTier,
    val icon: String, // emoji or asset: "🏆" , "models/trophy.glb"
    val unlockedAt: Long? = null, // null = locked
    val progress: Float = 0f, // 0..1
    val targetValue: Double? = null,
    val currentValue: Double? = null,
) {
    val isUnlocked: Boolean get() = unlockedAt != null
}

object AchievementDefinitions {
    val all = listOf(
        Achievement("first_workout", "First Rep", "Complete your first workout", AchievementCategory.MILESTONE, AchievementTier.BRONZE, "🎯", targetValue = 1.0),
        Achievement("pr_bench_100", "Century Bench", "Bench 100 kg", AchievementCategory.STRENGTH, AchievementTier.SILVER, "🏋️", targetValue = 100.0),
        Achievement("pr_any", "New PR", "Any new estimated 1RM", AchievementCategory.STRENGTH, AchievementTier.BRONZE, "💥"),
        Achievement("volume_10k", "10 Ton Club", "10,000 kg volume in a session", AchievementCategory.VOLUME, AchievementTier.SILVER, "📦", targetValue = 10000.0),
        Achievement("volume_100k", "100 Ton Month", "100,000 kg in 30 days", AchievementCategory.VOLUME, AchievementTier.GOLD, "🚚", targetValue = 100000.0),
        Achievement("streak_7", "Week Warrior", "7-day streak", AchievementCategory.CONSISTENCY, AchievementTier.SILVER, "🔥", targetValue = 7.0),
        Achievement("streak_30", "Month Forged", "30-day streak", AchievementCategory.CONSISTENCY, AchievementTier.GOLD, "⚒️", targetValue = 30.0),
        Achievement("body_logged", "Know Thyself", "Log weight + height", AchievementCategory.BODY, AchievementTier.BRONZE, "📏"),
        Achievement("perfect_week", "Adherence 100%", "7/7 sessions in a week", AchievementCategory.CONSISTENCY, AchievementTier.GOLD, "✅", targetValue = 7.0),
        Achievement("bmi_healthy", "In Range", "BMI 18.5–24.9", AchievementCategory.BODY, AchievementTier.SILVER, "⚖️"),
    )
}

fun evaluateAchievements(
    profile: UserProfile?,
    bodyMetrics: List<BodyMetric>,
    prs: List<PersonalRecord>,
    weeklyVolume: Double,
    streakDays: Int,
): List<Achievement> {
    val latest = bodyMetrics.maxByOrNull { it.measuredAt }
    val totalVolumeSession = weeklyVolume
    val benchPR = prs.find { it.exerciseId == "bench_bb" }?.weightKg ?: 0.0
    return AchievementDefinitions.all.map { def ->
        val (progress, unlocked) = when (def.id) {
            "body_logged" -> (if (latest?.weightKg != null && profile?.heightCm != null) 1f to true else 0f to false)
            "bmi_healthy" -> {
                val bmi = latest?.computeBmi() ?: profile?.bmi()
                val ok = bmi != null && bmi in 18.5..24.9
                (if (ok) 1f else 0.5f) to ok
            }
            "pr_bench_100" -> ( (benchPR / 100.0).coerceIn(0.0, 1.0).toFloat() ) to (benchPR >= 100)
            "streak_7" -> ( (streakDays / 7.0).coerceIn(0.0,1.0).toFloat() ) to (streakDays >= 7)
            "volume_10k" -> ( (totalVolumeSession / 10000.0).coerceIn(0.0,1.0).toFloat() ) to (totalVolumeSession >= 10000)
            else -> def.progress to (def.progress >= 1f)
        }
        def.copy(progress = progress, unlockedAt = if (unlocked) System.currentTimeMillis() else null)
    }
}
