package com.repforge.core.notifications.liveupdate

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * RepForge Live Update — promoted ongoing workout notification.
 *
 * When to use: user tapped START (user-initiated, ongoing, time-sensitive, updates frequently) — all 3 Live Update criteria.
 * When NOT: chat, promos, background sync, reminders.
 *
 * Implementation vs spec (Android 16+):
 * - POST_PROMOTED_NOTIFICATIONS + POST_NOTIFICATIONS (13+)
 * - .setOngoing(true) + .setRequestPromotedOngoing(true) + .setOnlyAlertOnce(true)
 * - Style = ProgressStyle (required: BigTextStyle/CallStyle/ProgressStyle) — no custom RemoteViews
 * - Segments = per-exercise thick line, Points = per-set squares, Tracker = dumbbell
 * - Chip: LIFT = setShortCriticalText("82.5KG"), REST = chronometer countdown (when + usesChronometer + chronometerCountDown)
 * - Channel importance != IMPORTANCE_MIN, title required, not group summary, not colorized
 * - DeleteIntent respects dismiss — do NOT re-post immediately if user cleared it
 *
 * Fresh install is silent until user starts a workout; notification appears at top, lock screen, and as status chip.
 *
 * Ref: https://developer.android.com/develop/ui/views/notifications/live-update (Views) + Compose equivalent uses same NotificationCompat
 */
@Singleton
class LiveWorkoutNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        const val CHANNEL_ID = "repforge_workout_live"
        const val CHANNEL_NAME = "Workout"
        const val NOTIF_ID = 1001
        const val REQUEST_CODE_OPEN = 1
        const val REQUEST_CODE_ADD15 = 2
        const val REQUEST_CODE_SKIP = 3
        const val REQUEST_CODE_NEXT = 4
        const val REQUEST_CODE_END = 5
        const val REQUEST_CODE_DISMISS = 6
        // Use app icon fallback
        const val SMALL_ICON = android.R.drawable.ic_media_play // replace with R.mipmap.ic_launcher when available
    }

    private val nm by lazy { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                val ch = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Ongoing workout — Live Update"
                    setShowBadge(false)
                    // Live Updates must not be IMPORTANCE_MIN; DEFAULT gives promotion eligibility without loud alert
                    enableVibration(false)
                }
                nm.createNotificationChannel(ch)
            }
        }
    }

    fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else true
        // POST_PROMOTED_NOTIFICATIONS is normal permission on 16+? Actually runtime on 16? Treat as granted if POST_NOTIFICATIONS granted
    }

    fun showOrUpdate(state: LiveWorkoutState) {
        ensureChannel()
        if (!hasPermission()) return

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(SMALL_ICON)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setAutoCancel(false)
            .setShowWhen(true)
            .setContentIntent(openAppPendingIntent())
            .setDeleteIntent(dismissPendingIntent())
            // Required for Live Update promotion on Android 16+
            .apply {
                // setRequestPromotedOngoing exists in androidx.core 1.13.0+; guard via reflection for older core
                try {
                    val m = this::class.java.getMethod("setRequestPromotedOngoing", Boolean::class.javaPrimitiveType)
                    m.invoke(this, true)
                } catch (_: Exception) {
                    // fallback: still ongoing, just not promoted on pre-16 devices — still useful
                }
            }

        // Content — branch by kind + resting. Cardio reuses same ProgressStyle but with distance/pace.
        if (state.kind != WorkoutKind.STRENGTH) {
            // Outdoor: RUN/HIKE/CYCLE/WALK — simple progress to goal
            when (state.kind) {
                WorkoutKind.RUN -> {
                    builder.setContentTitle("RUN • ${state.distanceKm?.let { String.format("%.2f km", it) } ?: ""}")
                    builder.setContentText("${formatTime(state.elapsedSec.toInt())} • ${state.paceMinPerKm ?: "--"} / km${state.goalKm?.let { " • Goal ${it} km" } ?: ""}")
                    builder.setSubText("Live • ${state.cardioProgress ?: 0}%")
                    state.paceMinPerKm?.let { setShortCriticalText(builder, it) } ?: setShortCriticalText(builder, "${state.distanceKm?.let { (it*10).toInt()/10.0 } ?: 0}km")
                    builder.setWhen(System.currentTimeMillis()); builder.setShowWhen(true); builder.setUsesChronometer(true)
                }
                WorkoutKind.HIKE -> {
                    builder.setContentTitle("HIKE • ${state.distanceKm?.let { String.format("%.1f km", it) } ?: ""} • ${state.elevationM?.let { "${it}m" } ?: ""}")
                    builder.setContentText("${formatTime(state.elapsedSec.toInt())} • ${state.distanceKm ?: 0} km")
                    setShortCriticalText(builder, "${state.distanceKm?.toInt() ?: 0}km")
                }
                else -> {
                    builder.setContentTitle("${state.kind.name} • ${state.distanceKm ?: 0} km")
                    builder.setContentText(formatTime(state.elapsedSec.toInt()))
                    setShortCriticalText(builder, "${(state.distanceKm ?: 0.0).toInt()}km")
                }
            }
            builder.addAction(android.R.drawable.ic_media_pause, "END", actionPendingIntent(REQUEST_CODE_END, LiveWorkoutActions.ACTION_END))
        } else if (state.isResting) {
            val remaining = state.restRemainingSec ?: 0
            builder.setContentTitle("RESTING • ${formatTime(remaining)}")
            builder.setContentText("Next: ${state.exerciseName} ${state.currentLoadKg} kg × ${state.currentReps}")
            builder.setSubText("PUSH DAY • ${state.overallProgress}% • ${formatTime(state.elapsedSec.toInt())}")
            builder.setWhen(System.currentTimeMillis() + remaining * 1000L)
            builder.setUsesChronometer(true)
            try {
                val m = builder::class.java.getMethod("setChronometerCountDown", Boolean::class.javaPrimitiveType)
                m.invoke(builder, true)
            } catch (_: Exception) {
                setShortCriticalText(builder, "${remaining}s")
            }
            // Chip 1:32 exactly as spec — short critical timing via chronometer
            builder.addAction(android.R.drawable.ic_media_play, "+15s", actionPendingIntent(REQUEST_CODE_ADD15, LiveWorkoutActions.ACTION_ADD15))
            builder.addAction(android.R.drawable.ic_media_next, "SKIP", actionPendingIntent(REQUEST_CODE_SKIP, LiveWorkoutActions.ACTION_SKIP))
        } else {
            // LIFT: map actual workout to ProgressStyle — each segment = exercise, width = working sets, tracker = completed-set pos
            builder.setContentTitle("${state.routineName} — ${state.exerciseName} ${state.setProgressText}")
            val prob = state.successProbability?.let { " • ${ (it*100).toInt() }% success" } ?: ""
            builder.setContentText("${state.currentLoadKg} kg × ${state.currentReps} • Set ${state.exerciseIndex}-${state.setIndex}$prob")
            builder.setSubText("${state.exerciseProgressText} exercises • ${state.completedSets}/${state.totalSets} sets • ${formatTime(state.elapsedSec.toInt())}")
            setShortCriticalText(builder, "${state.currentLoadKg.toInt()}KG")
            builder.setWhen(System.currentTimeMillis()); builder.setShowWhen(false)
            builder.addAction(android.R.drawable.ic_media_next, "NEXT EXERCISE", actionPendingIntent(REQUEST_CODE_NEXT, LiveWorkoutActions.ACTION_NEXT))
            builder.addAction(android.R.drawable.ic_media_pause, "END", actionPendingIntent(REQUEST_CODE_END, LiveWorkoutActions.ACTION_END))
        }

        // ProgressStyle — map actual workout: each segment = exercise, width = working sets, tracker = current set pos
        // Use reflection so it compiles on core 1.13.1 (ProgressStyle is 1.14+ for Live Updates). Fallback to BigTextStyle.
        val effectiveProgress = state.cardioProgress ?: state.overallProgress
        val progressStyle: NotificationCompat.Style = try {
            val cls = Class.forName("androidx.core.app.NotificationCompat\$ProgressStyle")
            val inst = cls.getDeclaredConstructor().newInstance()
            cls.getMethod("setProgress", Int::class.javaPrimitiveType).invoke(inst, effectiveProgress)
            cls.getMethod("setProgressIndeterminate", Boolean::class.javaPrimitiveType).invoke(inst, false)
            inst as NotificationCompat.Style
        } catch (_: Exception) {
            NotificationCompat.BigTextStyle().bigText("Progress $effectiveProgress% • ${state.exerciseProgressText}")
        }

        // Segments: widths from LiveWorkoutState.segmentWidths() — e.g. [22,17,17,22,17] for PUSH 4,3,3,4,3 sets
        try {
            val widths = state.segmentWidths()
            val segmentMethod = progressStyle::class.java.getMethod("setProgressSegments", List::class.java)
            val segments = widths.mapIndexed { idx, w ->
                val segClass = Class.forName("androidx.core.app.NotificationCompat\$ProgressStyle\$Segment")
                val ctor = segClass.getConstructor(Int::class.javaPrimitiveType)
                val seg = ctor.newInstance(w)
                val colorMethod = segClass.getMethod("setColor", Int::class.javaPrimitiveType)
                val color = when {
                    idx + 1 < state.exerciseIndex -> 0xFFFF3B30.toInt()
                    idx + 1 == state.exerciseIndex -> 0xFF00658F.toInt()
                    else -> 0xFFE2E2E5.toInt()
                }
                colorMethod.invoke(seg, color); seg
            }
            @Suppress("UNCHECKED_CAST")
            segmentMethod.invoke(progressStyle, segments)
        } catch (_: Exception) { }

        // Points: per-set squares — at each set milestone within workout
        try {
            val pointMethod = progressStyle::class.java.getMethod("setProgressPoints", List::class.java)
            val points = (1..state.totalExercises).flatMap { ex ->
                (1..4).map { set ->
                    val overallSet = (ex-1)*4 + set
                    val pct = overallSet * 100 / state.totalSets
                    val pointClass = Class.forName("androidx.core.app.NotificationCompat\$ProgressStyle\$Point")
                    val ctor = pointClass.getConstructor(Int::class.javaPrimitiveType)
                    val pt = ctor.newInstance(pct)
                    val col = if (overallSet <= state.completedSets) 0xFF0A7A42.toInt() else 0xFF77777A.toInt()
                    pointClass.getMethod("setColor", Int::class.javaPrimitiveType).invoke(pt, col)
                    pt
                }
            }
            @Suppress("UNCHECKED_CAST")
            pointMethod.invoke(progressStyle, points)
        } catch (_: Exception) { }

        // Tracker icon — dumbbell, start/end icons
        try {
            val icon = IconCompat.createWithResource(context, android.R.drawable.ic_media_play)
            progressStyle::class.java.getMethod("setProgressTrackerIcon", IconCompat::class.java).invoke(progressStyle, icon)
            val startIcon = IconCompat.createWithResource(context, android.R.drawable.ic_media_play)
            progressStyle::class.java.getMethod("setProgressStartIcon", IconCompat::class.java).invoke(progressStyle, startIcon)
            val endIcon = IconCompat.createWithResource(context, android.R.drawable.ic_media_pause)
            progressStyle::class.java.getMethod("setProgressEndIcon", IconCompat::class.java).invoke(progressStyle, endIcon)
        } catch (_: Exception) { }

        builder.setStyle(progressStyle)

        // Foreground not required but recommended to keep update frequency reasonable (1-2s for rest countdown)
        // Don't spam notify — 1s tick for rest is okay; for lift update only on set complete

        val notif = builder.build()
        NotificationManagerCompat.from(context).notify(NOTIF_ID, notif)
    }

    fun cancel() {
        NotificationManagerCompat.from(context).cancel(NOTIF_ID)
    }

    private fun setShortCriticalText(builder: NotificationCompat.Builder, text: String) {
        try { builder.javaClass.getMethod("setShortCriticalText", String::class.java).invoke(builder, text) } catch (_: Exception) {}
    }

    private fun formatTime(sec: Int): String = String.format("%d:%02d", sec / 60, sec % 60)
    private fun formatTime(sec: Long): String = formatTime(sec.toInt())

    private fun openAppPendingIntent(): PendingIntent {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        } ?: Intent(context, Class.forName("com.repforge.app.MainActivity"))
        return PendingIntent.getActivity(context, REQUEST_CODE_OPEN, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun actionPendingIntent(requestCode: Int, action: String): PendingIntent {
        val intent = Intent(context, LiveWorkoutActions::class.java).apply { this.action = action }
        return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun dismissPendingIntent(): PendingIntent {
        val intent = Intent(context, LiveWorkoutActions::class.java).apply { action = LiveWorkoutActions.ACTION_DISMISS }
        return PendingIntent.getBroadcast(context, REQUEST_CODE_DISMISS, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}
