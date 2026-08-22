package com.repforge.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class Gender { MALE, FEMALE, OTHER, UNSPECIFIED }

@Serializable
enum class Goal { STRENGTH, HYPERTROPHY, ENDURANCE, FAT_LOSS, GENERAL }

@Serializable
enum class ExperienceLevel { BEGINNER, INTERMEDIATE, ADVANCED, ELITE }

@Serializable
data class UserProfile(
    val id: String,
    val displayName: String = "Athlete",
    val avatarUrl: String? = null,
    val gender: Gender = Gender.UNSPECIFIED,
    val birthEpochMs: Long? = null,
    val heightCm: Double? = null, // 170.0
    val weightKg: Double? = null, // current, synced from latest BodyMetric
    val goal: Goal = Goal.STRENGTH,
    val experience: ExperienceLevel = ExperienceLevel.INTERMEDIATE,
    val trainingDaysPerWeek: Int = 4,
    val units: Units = Units.METRIC,
    val createdAt: Long,
    val updatedAt: Long,
) {
    fun ageYears(nowMs: Long = System.currentTimeMillis()): Int? {
        if (birthEpochMs == null) return null
        val years = (nowMs - birthEpochMs) / (365.25 * 24 * 3600 * 1000)
        return years.toInt()
    }
    fun bmi(): Double? {
        if (weightKg == null || heightCm == null || heightCm == 0.0) return null
        val h = heightCm / 100.0
        return weightKg / (h * h)
    }
}

@Serializable
enum class Units { METRIC, IMPERIAL }

fun Units.weightLabel() = if (this == Units.METRIC) "kg" else "lb"
fun Units.heightLabel() = if (this == Units.METRIC) "cm" else "in"
