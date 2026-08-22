package com.repforge.core.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private fun hasPermission() = if (android.os.Build.VERSION.SDK_INT >= 33) {
        androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
    } else true

    private fun openIntent() = context.packageManager.getLaunchIntentForPackage(context.packageName)?.let {
        PendingIntent.getActivity(context, 0, it.apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun notifyWorkoutReminder(routineName: String = "Push Day", id: Int = 2001) {
        if (!hasPermission()) return
        Channels.ensureAll(context)
        val n = NotificationCompat.Builder(context, Channels.WORKOUT_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Time to forge — $routineName")
            .setContentText("Your plan says today is $routineName • 7 exercises • ~62 min • Tap to start")
            .setAutoCancel(true)
            .setContentIntent(openIntent())
            .setOnlyAlertOnce(false)
            .build()
        NotificationManagerCompat.from(context).notify(id, n)
    }

    fun notifyRestComplete(nextExercise: String = "Bench Press", id: Int = 2002) {
        if (!hasPermission()) return
        Channels.ensureAll(context)
        val n = NotificationCompat.Builder(context, Channels.REST_TIMER)
            .setSmallIcon(android.R.drawable.ic_media_next)
            .setContentTitle("Rest done — go!")
            .setContentText("Next: $nextExercise • Tap to log")
            .setAutoCancel(true)
            .setContentIntent(openIntent())
            .setOnlyAlertOnce(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, n)
    }

    fun notifyAchievement(title: String, desc: String, id: Int = 2003) {
        if (!hasPermission()) return
        Channels.ensureAll(context)
        val n = NotificationCompat.Builder(context, Channels.ACHIEVEMENTS)
            .setSmallIcon(android.R.drawable.star_on)
            .setContentTitle("Achievement unlocked — $title")
            .setContentText(desc)
            .setAutoCancel(true)
            .setContentIntent(openIntent())
            .build()
        NotificationManagerCompat.from(context).notify(id, n)
    }

    fun notifyBodyReminder(id: Int = 2004) {
        if (!hasPermission()) return
        Channels.ensureAll(context)
        val n = NotificationCompat.Builder(context, Channels.BODY_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentTitle("Log weight today?")
            .setContentText("Update weight/height for BMI + normalized strength")
            .setAutoCancel(true)
            .setContentIntent(openIntent())
            .build()
        NotificationManagerCompat.from(context).notify(id, n)
    }

    fun notifyWeeklySummary(volumeKg: Double = 12400.0, prCount: Int = 2, id: Int = 2005) {
        if (!hasPermission()) return
        Channels.ensureAll(context)
        val n = NotificationCompat.Builder(context, Channels.WEEKLY_SUMMARY)
            .setSmallIcon(android.R.drawable.ic_menu_sort_by_size)
            .setContentTitle("Week forged")
            .setContentText("Volume ${"%.0f".format(volumeKg)} kg • $prCount PRs • tap for Progress")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Volume ${"%.0f".format(volumeKg)} kg • $prCount PRs • Streak 5 days • Next: increase bench to 85kg?"))
            .setAutoCancel(true)
            .setContentIntent(openIntent())
            .build()
        NotificationManagerCompat.from(context).notify(id, n)
    }
}
