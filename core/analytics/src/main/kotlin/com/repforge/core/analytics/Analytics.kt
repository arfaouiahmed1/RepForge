package com.repforge.core.analytics

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Analytics — never logs raw health values. Only product/engineering metrics.
 * Product: onboarding_completed, workout_started, set_logged, recommendation_shown, etc.
 * Engineering: startup latency, inference p50/p95, sync failures, etc.
 */
@Singleton
class Analytics @Inject constructor() {
    fun log(event: String, params: Map<String, String> = emptyMap()) {
        // Firebase Analytics impl — redacted raw values before logging
        // Firebase.analytics.logEvent(event, params.toBundle())
    }

    fun setUserProperty(name: String, value: String) { /* ... */ }

    object Events {
        const val ONBOARDING_COMPLETED = "onboarding_completed"
        const val WORKOUT_STARTED = "workout_started"
        const val WORKOUT_COMPLETED = "workout_completed"
        const val SET_LOGGED = "set_logged"
        const val RECOMMENDATION_SHOWN = "recommendation_shown"
        const val RECOMMENDATION_ACCEPTED = "recommendation_accepted"
        const val HEALTH_CONNECT_ENABLED = "health_connect_enabled"
        const val REST_TIMER_SKIPPED = "rest_timer_skipped"
    }
}

@Singleton
class PerformanceMonitor @Inject constructor() {
    fun trace(name: String, block: () -> Unit) { block() }
    fun recordInferenceLatency(p50: Long, p95: Long) { /* Firebase Performance */ }
}
