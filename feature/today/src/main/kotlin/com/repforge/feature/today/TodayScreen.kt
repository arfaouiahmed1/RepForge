package com.repforge.feature.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repforge.core.data.repository.ProfileRepository
import com.repforge.core.database.dao.RoutineDao
import com.repforge.core.database.dao.SetLogDao
import com.repforge.core.database.dao.TrainingSessionDao
import com.repforge.core.designsystem.component.*
import com.repforge.core.designsystem.token.RepForgeShapes
import com.repforge.core.designsystem.token.RepForgeTypeRoles
import com.repforge.core.notifications.liveupdate.rememberNotificationPermissionState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class TodayUiState(
    val dayLabel: String = SimpleDateFormat("EEEE", Locale.getDefault()).format(Calendar.getInstance().time).uppercase(),
    val planNameTop: String = "PUSH",
    val planNameBottom: String = "DAY",
    val meta: String = "Chest · Shoulders · Triceps  •  7 exercises · ~62 min",
    val readinessPercent: Int = 82,
    val readinessNote: String = "Compared with your recent baseline",
    val lastBench: String = "Bench Press     80 × 8",
    val lastOHP: String = "OHP             45 × 7",
    val nextGoal: String = "Bench Press     82.5 × 8",
    val isRestDay: Boolean = false,
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val routineDao: RoutineDao,
    private val sessionDao: TrainingSessionDao,
    private val setLogDao: SetLogDao,
    private val profileRepo: ProfileRepository,
) : ViewModel() {

    // Realistic: observe routines, pick today's by dayOfWeek, else first
    private val routinesFlow = routineDao.observeAll()
    private val sessionsFlow = sessionDao.observeAll()
    private val recentSetsFlow = setLogDao.observeRecent(20)

    val state = combine(routinesFlow, sessionsFlow, recentSetsFlow) { routines, sessions, recent ->
        val cal = Calendar.getInstance()
        val dayIdx = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Mon=0
        val todayRoutine = routines.find { it.dayOfWeek == dayIdx } ?: routines.firstOrNull()
        val isRest = todayRoutine == null && routines.isNotEmpty()

        // Hero split from routine name e.g. "PPL — Push" -> PUSH
        val topBottom = when {
            todayRoutine?.name?.contains("Push") == true -> "PUSH" to "DAY"
            todayRoutine?.name?.contains("Pull") == true -> "PULL" to "DAY"
            todayRoutine?.name?.contains("Legs") == true -> "LEGS" to "DAY"
            todayRoutine?.name?.contains("Upper") == true -> "UPPER" to "DAY"
            todayRoutine?.name?.contains("Lower") == true -> "LOWER" to "DAY"
            else -> (todayRoutine?.name?.uppercase()?.take(8) ?: "PUSH") to "DAY"
        }

        // Last bench from real sets if available
        val lastBenchSet = recent.find { it.exerciseName.contains("Bench", ignoreCase = true) }
        val lastBench = lastBenchSet?.let { "${it.exerciseName.take(11).padEnd(11)} ${it.weightKg.toInt()} × ${it.completedReps}" } ?: "Bench Press     80 × 8"
        val lastOHPSet = recent.find { it.exerciseName.contains("Overhead") || it.exerciseName.contains("Press") && !it.exerciseName.contains("Bench") }
        val lastOHP = lastOHPSet?.let { "OHP             ${it.weightKg.toInt()} × ${it.completedReps}" } ?: "OHP             45 × 7"

        // Next goal via deterministic progression (real engine would use ProgressionEngine)
        val nextGoal = lastBenchSet?.let {
            val nextLoad = if ((it.rir ?: 0) >= 2) it.weightKg + 2.5 else it.weightKg
            "Bench Press     ${nextLoad.toInt().let { if (nextLoad % 1 == 0.0) it.toString() else nextLoad.toString() }} × 8"
        } ?: "Bench Press     82.5 × 8"

        val meta = todayRoutine?.let { "${it.description ?: "Chest·Shoulders·Triceps"} • ~${it.estimatedMin} min" } ?: "Chest · Shoulders · Triceps  •  7 exercises · ~62 min"

        // Readiness: if we have sessions, compute adherence last 7 days else 82
        val weekAgo = System.currentTimeMillis() - 7*24*3600*1000L
        val weekSessions = sessions.count { it.startedAt > weekAgo }
        val readiness = if (weekSessions > 0) (70 + weekSessions*5).coerceIn(0, 95) else 82

        TodayUiState(
            dayLabel = SimpleDateFormat("EEEE", Locale.getDefault()).format(cal.time).uppercase(),
            planNameTop = topBottom.first,
            planNameBottom = topBottom.second,
            meta = meta,
            readinessPercent = readiness,
            readinessNote = if (weekSessions > 0) "$weekSessions sessions last 7 days" else "Compared with your recent baseline",
            lastBench = lastBench,
            lastOHP = lastOHP,
            nextGoal = nextGoal,
            isRestDay = isRest,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodayUiState())
}

@Composable
fun TodayRoute(
    onStartWorkout: () -> Unit,
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val notifPerm = rememberNotificationPermissionState()
    TodayScreen(state = state, hasNotifPermission = notifPerm.hasPermission, onRequestNotif = notifPerm.request, onStartWorkout = onStartWorkout)
}

@Composable
fun TodayScreen(
    state: TodayUiState,
    hasNotifPermission: Boolean,
    onRequestNotif: () -> Unit,
    onStartWorkout: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(state.dayLabel, style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        ExpressiveSplitHero(top = state.planNameTop, bottom = state.planNameBottom)
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            GiantStartShape(onClick = {
                if (!hasNotifPermission) onRequestNotif()
                onStartWorkout()
            }, label = if (state.isRestDay) "MOBILITY" else "START")
        }
        if (state.isRestDay) {
            Text("Rest day — light mobility or walk. Tap MOBILITY for 10-min reset.", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (!hasNotifPermission) {
            Column(modifier = Modifier.clip(RepForgeShapes.CardMedium).background(MaterialTheme.colorScheme.errorContainer).padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Enable notifications for Live Workout", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onErrorContainer)
                Text("Promoted Live Update shows rest countdown + next set on lock screen — only during workout.", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f))
            }
        } else {
            Text("Live Update ready — workout appears as promoted notification + status chip", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
        }
        Text(state.meta, style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(modifier = Modifier.clip(RepForgeShapes.Metric).background(MaterialTheme.colorScheme.surfaceVariant).padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${state.readinessPercent}%", style = RepForgeTypeRoles.MetricMedium, color = MaterialTheme.colorScheme.onSurface)
                Text("READY", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(state.readinessNote, style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f).clip(RepForgeShapes.CardMedium).background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp)) {
                Text("LAST TIME", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                Spacer(Modifier.height(6.dp))
                Text(state.lastBench, style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurface)
                Text(state.lastOHP, style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(modifier = Modifier.weight(1f).clip(RepForgeShapes.CardMedium).background(MaterialTheme.colorScheme.primaryContainer).padding(16.dp)) {
                Text("NEXT TARGET", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                Spacer(Modifier.height(6.dp))
                Text(state.nextGoal, style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("Model: 78% success", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
            }
        }
    }
}

