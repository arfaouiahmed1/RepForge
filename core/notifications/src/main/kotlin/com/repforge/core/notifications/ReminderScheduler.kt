package com.repforge.core.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context,
) {
    private val wm = WorkManager.getInstance(context)

    fun scheduleDailyReminder(hour: Int = 18, minute: Int = 0) {
        val req = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay(hour, minute), TimeUnit.MILLISECONDS)
            .addTag("daily_reminder")
            .build()
        wm.enqueueUniquePeriodicWork("daily_reminder", ExistingPeriodicWorkPolicy.UPDATE, req)
    }

    fun scheduleBodyReminderWeekly() {
        val req = PeriodicWorkRequestBuilder<BodyReminderWorker>(7, TimeUnit.DAYS).addTag("body_reminder").build()
        wm.enqueueUniquePeriodicWork("body_reminder", ExistingPeriodicWorkPolicy.KEEP, req)
    }

    fun scheduleWeeklySummary() {
        val req = PeriodicWorkRequestBuilder<WeeklySummaryWorker>(7, TimeUnit.DAYS).addTag("weekly_summary").build()
        wm.enqueueUniquePeriodicWork("weekly_summary", ExistingPeriodicWorkPolicy.KEEP, req)
    }

    fun cancelAll() {
        wm.cancelAllWorkByTag("daily_reminder")
        wm.cancelAllWorkByTag("body_reminder")
        wm.cancelAllWorkByTag("weekly_summary")
    }

    private fun initialDelay(hour: Int, minute: Int): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, hour)
        cal.set(java.util.Calendar.MINUTE, minute)
        cal.set(java.util.Calendar.SECOND, 0)
        if (cal.timeInMillis <= System.currentTimeMillis()) cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
        return cal.timeInMillis - System.currentTimeMillis()
    }
}

@HiltWorker
class DailyReminderWorker @AssistedInject constructor(@Assisted ctx: Context, @Assisted params: WorkerParameters, private val notifier: ReminderNotifier) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result { notifier.notifyWorkoutReminder(); return Result.success() }
}

@HiltWorker
class BodyReminderWorker @AssistedInject constructor(@Assisted ctx: Context, @Assisted params: WorkerParameters, private val notifier: ReminderNotifier) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result { notifier.notifyBodyReminder(); return Result.success() }
}

@HiltWorker
class WeeklySummaryWorker @AssistedInject constructor(@Assisted ctx: Context, @Assisted params: WorkerParameters, private val notifier: ReminderNotifier) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result { notifier.notifyWeeklySummary(); return Result.success() }
}
