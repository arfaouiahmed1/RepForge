package com.repforge.core.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.WeightRecord
import com.repforge.core.model.HealthSnapshot
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client by lazy { HealthConnectClient.getOrCreate(context) }

    val permissions = setOf(
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getWritePermission(androidx.health.connect.client.records.ExerciseSessionRecord::class),
    )

    suspend fun isAvailable(): Boolean =
        HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    suspend fun hasPermissions(): Boolean =
        try { client.permissionController.getGrantedPermissions().containsAll(permissions) } catch (_: Exception) { false }

    suspend fun readSnapshot(daysBack: Long = 7): HealthSnapshot? {
        if (!isAvailable() || !hasPermissions()) return null
        return try {
            // Stub for demo — real impl reads WeightRecord and SleepSessionRecord via client.readRecords
            HealthSnapshot(java.time.LocalDate.now().toString(), weightKg = null, sleepHours = null, restingHr = null, heartRate = null)
        } catch (_: Exception) { null }
    }

    suspend fun writeWorkout(start: Instant, end: Instant, title: String) {
        if (!isAvailable() || !hasPermissions()) return
        // Stub — real impl inserts ExerciseSessionRecord via client.insertRecords
        // Keep empty for demo to avoid internal constructor issues on 1.1.0
    }
}
