package com.repforge.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object Channels {
    const val WORKOUT_LIVE = "repforge_workout_live" // Live Update — already in LiveWorkoutNotifier
    const val WORKOUT_REMINDERS = "repforge_reminders"
    const val REST_TIMER = "repforge_rest"
    const val ACHIEVEMENTS = "repforge_achievements"
    const val BODY_REMINDERS = "repforge_body"
    const val WEEKLY_SUMMARY = "repforge_weekly"

    fun ensureAll(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        listOf(
            NotificationChannel(WORKOUT_REMINDERS, "Workout reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Daily training reminder — e.g. Push Day at 18:00"
                setShowBadge(false)
            },
            NotificationChannel(REST_TIMER, "Rest timer", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Rest complete — time for next set"
                setShowBadge(false)
                enableVibration(true)
            },
            NotificationChannel(ACHIEVEMENTS, "Achievements", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "PRs, streaks, volume unlocked"
            },
            NotificationChannel(BODY_REMINDERS, "Body metrics", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Weekly weight/height log nudge"
                setShowBadge(false)
            },
            NotificationChannel(WEEKLY_SUMMARY, "Weekly summary", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Sunday progress digest"
            },
        ).forEach { if (nm.getNotificationChannel(it.id) == null) nm.createNotificationChannel(it) }
        // WORKOUT_LIVE handled by LiveWorkoutNotifier.ensureChannel()
    }
}
