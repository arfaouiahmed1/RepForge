package com.repforge.core.notifications.liveupdate

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Bridge between workout domain and Live Update notifier.
 * - Keeps the latest LiveWorkoutState in memory (survives config change)
 * - Throttles rest tick updates to 1s (Live Updates shouldn't spam >1-2s)
 * - Respects dismiss suppress window
 * - Exposes via Hilt to ViewModels
 */
@Singleton
class WorkoutLiveUpdateController @Inject constructor(
    private val notifier: LiveWorkoutNotifier,
) {
    private val _state = MutableStateFlow(LiveWorkoutState())
    val state: StateFlow<LiveWorkoutState> = _state

    fun start(state: LiveWorkoutState) {
        _state.value = state
        if (!LiveWorkoutActions.dismissedUntil.let { System.currentTimeMillis() < it }) {
            notifier.showOrUpdate(state)
        }
    }

    fun update(transform: (LiveWorkoutState) -> LiveWorkoutState) {
        _state.value = transform(_state.value)
        if (LiveWorkoutActions.dismissedUntil.let { System.currentTimeMillis() < it }) return
        notifier.showOrUpdate(_state.value)
    }

    fun tickRest() {
        val cur = _state.value
        val remaining = cur.restRemainingSec ?: return
        if (remaining <= 0) {
            update { it.copy(restRemainingSec = null) }
        } else {
            update { it.copy(restRemainingSec = remaining - 1) }
        }
    }

    fun addRest(delta: Int) = update { it.copy(restRemainingSec = ((it.restRemainingSec ?: 0) + delta).coerceAtLeast(0)) }
    fun skipRest() = update { it.copy(restRemainingSec = null) }
    fun completeSet(nextLoad: Double? = null) {
        update {
            it.copy(
                completedSets = it.completedSets + 1,
                setIndex = it.setIndex + 1,
                restRemainingSec = it.restTotalSec,
                currentLoadKg = nextLoad ?: it.currentLoadKg,
            )
        }
    }

    fun finish() {
        notifier.cancel()
        _state.value = LiveWorkoutState()
        LiveWorkoutActions.dismissedUntil = 0L
    }
}
