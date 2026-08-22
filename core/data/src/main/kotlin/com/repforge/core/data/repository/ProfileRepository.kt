package com.repforge.core.data.repository

import com.repforge.core.database.dao.UserProfileDao
import com.repforge.core.database.dao.BodyMetricDao
import com.repforge.core.database.entity.BodyMetricEntity
import com.repforge.core.database.entity.UserProfileEntity
import com.repforge.core.model.BodyMetric
import com.repforge.core.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import java.util.UUID

@Singleton
class ProfileRepository @Inject constructor(
    private val profileDao: UserProfileDao,
    private val bodyDao: BodyMetricDao,
) {
    fun observeProfile(): Flow<UserProfile?> = profileDao.observe().map { it?.toModel() }
    fun observeBodyMetrics(): Flow<List<BodyMetric>> = bodyDao.observeAll().map { list -> list.map { it.toModel() } }
    fun observeLatestBody(): Flow<BodyMetric?> = bodyDao.observeLatest().map { it?.toModel() }

    suspend fun getProfile(): UserProfile? = profileDao.get()?.toModel()

    suspend fun upsertProfile(profile: UserProfile) {
        profileDao.upsert(profile.toEntity())
        // Also log body metric if weight/height changed
        if (profile.weightKg != null || profile.heightCm != null) {
            val bmi = profile.bmi()
            bodyDao.upsert(
                BodyMetricEntity(
                    id = UUID.randomUUID().toString(),
                    weightKg = profile.weightKg,
                    heightCm = profile.heightCm,
                    bodyFatPct = null,
                    muscleMassKg = null,
                    bmi = bmi,
                    measuredAt = System.currentTimeMillis(),
                    source = "profile"
                )
            )
        }
    }

    suspend fun logBodyMetric(weightKg: Double?, heightCm: Double?, bodyFatPct: Double? = null): BodyMetric {
        val height = heightCm ?: getProfile()?.heightCm
        val bmi = if (weightKg != null && height != null && height != 0.0) weightKg / ((height/100)*(height/100)) else null
        val entity = BodyMetricEntity(
            id = UUID.randomUUID().toString(),
            weightKg = weightKg,
            heightCm = heightCm,
            bodyFatPct = bodyFatPct,
            muscleMassKg = null,
            bmi = bmi,
            measuredAt = System.currentTimeMillis(),
            source = "manual"
        )
        bodyDao.upsert(entity)
        // also update profile weight/height if provided
        val p = getProfile()
        if (p != null && (weightKg != null || heightCm != null)) {
            upsertProfile(p.copy(weightKg = weightKg ?: p.weightKg, heightCm = heightCm ?: p.heightCm, updatedAt = System.currentTimeMillis()))
        }
        return entity.toModel()
    }
}

private fun UserProfile.toEntity() = UserProfileEntity(
    id = id, displayName = displayName, gender = gender.name, birthEpochMs = birthEpochMs,
    heightCm = heightCm, weightKg = weightKg, goal = goal.name, experience = experience.name,
    trainingDaysPerWeek = trainingDaysPerWeek, units = units.name, createdAt = createdAt, updatedAt = updatedAt
)
private fun UserProfileEntity.toModel() = UserProfile(
    id = id, displayName = displayName, gender = try { com.repforge.core.model.Gender.valueOf(gender) } catch(_:Exception){ com.repforge.core.model.Gender.UNSPECIFIED },
    birthEpochMs = birthEpochMs, heightCm = heightCm, weightKg = weightKg,
    goal = try { com.repforge.core.model.Goal.valueOf(goal) } catch(_:Exception){ com.repforge.core.model.Goal.STRENGTH },
    experience = try { com.repforge.core.model.ExperienceLevel.valueOf(experience) } catch(_:Exception){ com.repforge.core.model.ExperienceLevel.INTERMEDIATE },
    trainingDaysPerWeek = trainingDaysPerWeek, units = try { com.repforge.core.model.Units.valueOf(units) } catch(_:Exception){ com.repforge.core.model.Units.METRIC },
    createdAt = createdAt, updatedAt = updatedAt
)
private fun BodyMetricEntity.toModel() = BodyMetric(id = id, weightKg = weightKg, heightCm = heightCm, bodyFatPct = bodyFatPct, muscleMassKg = muscleMassKg, bmi = bmi, measuredAt = measuredAt, source = source)
