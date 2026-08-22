package com.repforge.app

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.repforge.core.notifications.liveupdate.LiveWorkoutState

/**
 * Health foreground-service for long-running fitness tracking.
 * Isolated — don't sprinkle foreground services. Use only while actively tracking sensors (GPS for outdoor, HR for strength).
 * Android 16 tightened health permission handling: needs FOREGROUND_SERVICE_HEALTH + proper type.
 */
class WorkoutForegroundService : Service() {
    companion object {
        const val ACTION_START = "repforge.action.START_FG"
        const val ACTION_STOP = "repforge.action.STOP_FG"
        var isRunning = false
            private set
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val notif = buildForegroundNotification()
                // health type for fitness tracking — required on 14+ for long-running exercise
                if (android.os.Build.VERSION.SDK_INT >= 34) {
                    startForeground(1001, notif, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
                } else {
                    startForeground(1001, notif)
                }
                isRunning = true
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                isRunning = false
            }
        }
        return START_NOT_STICKY
    }

    private fun buildForegroundNotification(): Notification {
        // Reuse LiveWorkoutNotifier's channel but as plain ongoing for <16 fallback
        // On 16+ this is still the Live Update notification; service just keeps process alive for sensor tracking
        return NotificationCompat.Builder(this, "repforge_workout_live")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Workout ongoing")
            .setContentText("Tracking workout — tap to return")
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
