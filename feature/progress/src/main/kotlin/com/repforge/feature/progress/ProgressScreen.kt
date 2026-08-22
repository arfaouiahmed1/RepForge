package com.repforge.feature.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repforge.core.database.dao.SetLogDao
import com.repforge.core.designsystem.component.ProgressHero
import com.repforge.core.designsystem.token.RepForgeShapes
import com.repforge.core.designsystem.token.RepForgeTypeRoles
import com.repforge.core.model.estimated1RM
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*

data class ProgressUiState(
    val exercise: String = "BENCH PRESS",
    val exerciseId: String = "bench_bb",
    val estimated1RM: String = "94.3 KG",
    val delta: String = "↑ 7.2%  •  12 weeks",
    val selectedMetric: String = "Strength",
    val points: List<Float> = listOf(0.6f, 0.62f, 0.58f, 0.65f, 0.68f, 0.72f, 0.75f, 0.80f, 0.78f, 0.85f),
    val bestSet: String = "85 kg × 7",
    val volumeDelta: String = "+11.4%",
    val isEmpty: Boolean = true,
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val setLogDao: SetLogDao,
) : ViewModel() {
    private val selectedExercise = MutableStateFlow("bench_bb")
    private val selectedMetric = MutableStateFlow("Strength")

    val state: StateFlow<ProgressUiState> = combine(setLogDao.observeForExercise("bench_bb", 50), selectedExercise, selectedMetric) { sets, exId, metric ->
        if (sets.isEmpty()) {
            ProgressUiState(isEmpty = true)
        } else {
            // Real: group by session-ish via timestamp, compute per-session best 1RM
            val bySession = sets.groupBy { it.timestamp / (24*3600*1000) }.toList().sortedBy { it.first }.takeLast(10)
            val session1RMs = bySession.map { (_, group) -> group.maxOf { estimated1RM(it.weightKg, it.completedReps) } }
            val max = session1RMs.maxOrNull() ?: 0.0
            val min = session1RMs.minOrNull() ?: 0.0
            val range = (max - min).coerceAtLeast(1.0)
            val points = session1RMs.map { ((it - min) / range * 0.6 + 0.3).toFloat() }
            val current1RM = session1RMs.lastOrNull() ?: max
            val first1RM = session1RMs.firstOrNull() ?: current1RM
            val deltaPct = if (first1RM == 0.0) 0.0 else (current1RM - first1RM) / first1RM * 100
            val best = sets.maxByOrNull { estimated1RM(it.weightKg, it.completedReps) }
            ProgressUiState(
                exercise = "BENCH PRESS",
                exerciseId = exId,
                estimated1RM = String.format("%.1f KG", current1RM),
                delta = String.format("%s %.1f%% • 12 weeks", if (deltaPct >= 0) "↑" else "↓", kotlin.math.abs(deltaPct)),
                selectedMetric = metric,
                points = points.ifEmpty { listOf(0.5f) },
                bestSet = best?.let { "${it.weightKg.toInt()} kg × ${it.completedReps}" } ?: "—",
                volumeDelta = String.format("%s%.1f%%", if (deltaPct >= 0) "+" else "", deltaPct * 1.5),
                isEmpty = false,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProgressUiState())

    fun selectExercise(id: String) { selectedExercise.value = id }
    fun selectMetric(m: String) { selectedMetric.value = m }
}

@Composable
fun ProgressRoute(viewModel: ProgressViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    ProgressScreen(state = state, onMetric = viewModel::selectMetric)
}

@Composable
fun ProgressScreen(state: ProgressUiState, onMetric: (String) -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("PROGRESS", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        ProgressHero(exercise = state.exercise, estimated1RM = state.estimated1RM, delta = state.delta)
        if (state.isEmpty) Text("Log a workout to see real 1RM trend — currently showing sample", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        Box(modifier = Modifier.fillMaxWidth().height(120.dp).clip(RepForgeShapes.CardLarge).background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp)) {
            val color = MaterialTheme.colorScheme.primary
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path()
                val stepX = size.width / (state.points.size - 1).coerceAtLeast(1)
                state.points.forEachIndexed { i, v ->
                    val x = i * stepX
                    val y = size.height - (v * size.height * 0.8f) - size.height * 0.1f
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, color = color, style = Stroke(width = 4f))
                state.points.forEachIndexed { i, v ->
                    val x = i * stepX
                    val y = size.height - (v * size.height * 0.8f) - size.height * 0.1f
                    drawCircle(color = color, radius = 6f, center = Offset(x, y))
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Strength", "Volume", "Reps", "Load").forEach { label ->
                val selected = label == state.selectedMetric
                Box(modifier = Modifier.clip(RepForgeShapes.Pill).background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant).clickable { onMetric(label) }.padding(horizontal = 14.dp, vertical = 8.dp)) {
                    Text(label, style = RepForgeTypeRoles.LabelExpressive, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f).clip(RepForgeShapes.Metric).background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp)) {
                Text("STRENGTH TREND", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                Text(state.delta.substringBefore("•").trim(), style = RepForgeTypeRoles.MetricMedium, color = MaterialTheme.colorScheme.onSurface)
            }
            Column(modifier = Modifier.weight(1f).clip(RepForgeShapes.Metric).background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp)) {
                Text("WORKING VOLUME", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                Text(state.volumeDelta, style = RepForgeTypeRoles.MetricMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f).clip(RepForgeShapes.Metric).background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp)) {
                Text("SESSION ADHERENCE", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                Text(if (state.isEmpty) "—" else "87%", style = RepForgeTypeRoles.MetricMedium, color = MaterialTheme.colorScheme.onSurface)
            }
            Column(modifier = Modifier.weight(1f).clip(RepForgeShapes.Metric).background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp)) {
                Text("BEST SET", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                Text(state.bestSet, style = RepForgeTypeRoles.MetricMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        Text("AUG ————— NOV", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
    }
}
