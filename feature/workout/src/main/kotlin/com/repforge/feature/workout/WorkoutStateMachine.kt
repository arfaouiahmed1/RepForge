package com.repforge.feature.workout

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Critical state machine per spec:
 * NOT_STARTED → ACTIVE { SET_IN_PROGRESS, RESTING, PAUSED } → COMPLETED
 * Persist continuously — if OS kills process mid-workout, reopen into session.
 */
enum class WorkoutPhase { NOT_STARTED, SET_IN_PROGRESS, RESTING, PAUSED, COMPLETED }

data class WorkoutSessionState(
    val phase: WorkoutPhase = WorkoutPhase.NOT_STARTED,
    val sessionId: String? = null,
    val exerciseIndex: Int = 0,
    val setIndex: Int = 0,
    val restRemaining: Int? = null,
    val startedAt: Long? = null,
)

class WorkoutStateMachine {
    private val _state = MutableStateFlow(WorkoutSessionState())
    val state: StateFlow<WorkoutSessionState> = _state

    fun start(sessionId: String) {
        _state.value = WorkoutSessionState(phase = WorkoutPhase.SET_IN_PROGRESS, sessionId = sessionId, startedAt = System.currentTimeMillis())
    }
    fun completeSet(restSeconds: Int = 90) {
        _state.value = _state.value.copy(phase = WorkoutPhase.RESTING, restRemaining = restSeconds)
    }
    fun tickRest() {
        val r = _state.value.restRemaining ?: return
        if (r <= 1) _state.value = _state.value.copy(phase = WorkoutPhase.SET_IN_PROGRESS, restRemaining = null)
        else _state.value = _state.value.copy(restRemaining = r - 1)
    }
    fun skipRest() { _state.value = _state.value.copy(phase = WorkoutPhase.SET_IN_PROGRESS, restRemaining = null) }
    fun addRest(delta: Int) { _state.value = _state.value.copy(restRemaining = ((_state.value.restRemaining ?: 0) + delta).coerceAtLeast(0)) }
    fun pause() { if (_state.value.phase == WorkoutPhase.SET_IN_PROGRESS) _state.value = _state.value.copy(phase = WorkoutPhase.PAUSED) }
    fun resume() { if (_state.value.phase == WorkoutPhase.PAUSED) _state.value = _state.value.copy(phase = WorkoutPhase.SET_IN_PROGRESS) }
    fun finish() { _state.value = _state.value.copy(phase = WorkoutPhase.COMPLETED) }
}
