package com.repforge.core.data.repository

import com.repforge.core.database.dao.AchievementDao
import com.repforge.core.database.entity.AchievementEntity
import com.repforge.core.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepository @Inject constructor(
    private val dao: AchievementDao,
    private val profileRepo: ProfileRepository,
) {
    fun observe(): Flow<List<Achievement>> = dao.observeAll().map { list -> list.map { it.toModel() } }

    suspend fun seedIfEmpty() {
        if (dao.observeAll().first().isNotEmpty()) return
        dao.upsertAll(AchievementDefinitions.all.map { it.toEntity() })
    }

    suspend fun evaluateAndUpdate(
        prs: List<PersonalRecord>,
        weeklyVolume: Double,
        streakDays: Int,
    ) {
        seedIfEmpty()
        val profile = profileRepo.getProfile()
        val metrics = profileRepo.observeBodyMetrics().first()
        val evaluated = evaluateAchievements(profile, metrics, prs, weeklyVolume, streakDays)
        dao.upsertAll(evaluated.map { it.toEntity() })
    }
}

private fun Achievement.toEntity() = AchievementEntity(
    id = id, title = title, description = description, category = category.name, tier = tier.name, icon = icon, unlockedAt = unlockedAt, progress = progress, targetValue = targetValue
)
private fun AchievementEntity.toModel() = Achievement(
    id = id, title = title, description = description,
    category = try { AchievementCategory.valueOf(category) } catch(_:Exception){ AchievementCategory.MILESTONE },
    tier = try { AchievementTier.valueOf(tier) } catch(_:Exception){ AchievementTier.BRONZE },
    icon = icon, unlockedAt = unlockedAt, progress = progress, targetValue = targetValue
)
