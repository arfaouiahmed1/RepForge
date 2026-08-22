package com.repforge.core.notifications.liveupdate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Handles Live Update actions + dismiss.
 * - Dismiss: user swiped away — do NOT re-post immediately (best practice). Set a flag to suppress for 5 min or until next user action.
 * - +15 / SKIP / NEXT / END: delegated to WorkoutSessionManager via broadcast or directly via repository.
 *
 * Keep intents short, no custom RemoteViews, and keep actions to 2 max for Live Update readability.
 */
@AndroidEntryPoint
class LiveWorkoutActions : BroadcastReceiver() {

    @Inject lateinit var notifier: LiveWorkoutNotifier

    // Optional: inject WorkoutRepository to actually mutate state
    // @Inject lateinit var workoutRepository: WorkoutRepository

    companion object {
        const val ACTION_ADD15 = "com.repforge.action.ADD15"
        const val ACTION_SKIP = "com.repforge.action.SKIP"
        const val ACTION_NEXT = "com.repforge.action.NEXT"
        const val ACTION_END = "com.repforge.action.END"
        const val ACTION_DISMISS = "com.repforge.action.DISMISS"
        // In-memory suppress flag — survives until process death; persist to DataStore if needed
        @Volatile var dismissedUntil: Long = 0L
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_ADD15 -> {
                // TODO: workoutRepository.addRest(15)
                // Re-post with updated state — caller should invoke notifier.showOrUpdate(newState)
            }
            ACTION_SKIP -> {
                // TODO: workoutRepository.skipRest()
            }
            ACTION_NEXT -> {
                // TODO: workoutRepository.nextExercise()
            }
            ACTION_END -> {
                // TODO: workoutRepository.finishSession()
                notifier.cancel()
            }
            ACTION_DISMISS -> {
                dismissedUntil = System.currentTimeMillis() + 5 * 60 * 1000L
                // Do not re-notify until next explicit state change or 5 min passes
            }
        }
    }

    fun shouldSuppress(): Boolean = System.currentTimeMillis() < dismissedUntil
}
