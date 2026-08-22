package com.repforge.core.model

import java.util.Calendar

enum class SessionStatus { PLANNED, COMPLETED, MISSED, DELOAD, SKIPPED }

data class CalendarSession(val date: String, val routineId: String?, val status: SessionStatus, val routineName: String? = null)

object ProgramCalendar {
    fun missedHandling(session: CalendarSession, action: String): List<CalendarSession> {
        // Move it to Wednesday, Skip, Reschedule week — intelligently shift, don't destroy schedule
        return when (action) {
            "MOVE" -> listOf(session.copy(date = nextDay(session.date)))
            "SKIP" -> listOf(session.copy(status = SessionStatus.SKIPPED))
            "RESCHEDULE_WEEK" -> listOf(session) // TODO: shift week
            else -> listOf(session)
        }
    }
    private fun nextDay(date: String): String = date // stub
    fun adherence(sessions: List<CalendarSession>): Double {
        val planned = sessions.count { it.status != SessionStatus.DELOAD }
        val done = sessions.count { it.status == SessionStatus.COMPLETED }
        return if (planned == 0) 0.0 else done.toDouble() / planned
    }
}
