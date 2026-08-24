package com.repforge.feature.progress

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repforge.core.data.repository.AchievementRepository
import com.repforge.core.database.dao.ExerciseDao
import com.repforge.core.database.dao.SetLogDao
import com.repforge.core.designsystem.component.ProgressHero
import com.repforge.core.designsystem.icon.RepForgeSymbolRole
import com.repforge.core.designsystem.icon.RepForgeSymbols
import com.repforge.core.designsystem.token.RepForgeMotion
import com.repforge.core.designsystem.token.RepForgeShapes
import com.repforge.core.designsystem.token.RepForgeTypeRoles
import com.repforge.core.model.Achievement
import com.repforge.core.model.deltaPercent
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Progress UI state (todo 13). All defaults are neutral — no fabricated names,
 * values, or series. An empty database and an exercise without work sets are
 * distinct states so the UI never invents data.
 */
data class ProgressUiState(
    val exercise: String = "",
    val exerciseId: String = "",
    val heroValue: String = "",
    val heroLabel: String = "",
    val delta: String = "",
    val selectedMetric: String = ProgressMetric.STRENGTH.label,
    val points: List<Float> = emptyList(),
    val bestSet: String = "-",
    val volumeByMuscle: List<Pair<String, String>> = emptyList(),
    /** True when the whole set-log history is empty. */
    val isEmpty: Boolean = true,
    /** False when history exists but the selected exercise has no work sets yet. */
    val hasExerciseData: Boolean = false,
    /** TalkBack description of what the chart plots; empty when there is nothing to plot. */
    val chartDescription: String = "",
    val achievements: List<Achievement> = emptyList(),
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val setLogDao: SetLogDao,
    exerciseDao: ExerciseDao,
    achievementRepository: AchievementRepository,
) : ViewModel() {
    private val selectedExercise = MutableStateFlow("")
    private val selectedMetric = MutableStateFlow(ProgressMetric.STRENGTH.label)

    // Locale-aware number formatting is deliberate: kg/tonne/reps figures follow
    // the device locale (decimal separator, grouping) rather than a hardcoded US format.
    private val locale: Locale = Locale.getDefault()

    val state: StateFlow<ProgressUiState> = combine(
        setLogDao.observeRecent(200),
        exerciseDao.observeAll(),
        achievementRepository.observe(),
        selectedMetric,
        selectedExercise,
    ) { recent, exercises, achievements, metricLabel, requestedExerciseId ->
        val metric = ProgressMetric.fromLabel(metricLabel)
        val exerciseId = requestedExerciseId.ifEmpty {
            recent.firstOrNull { !it.isWarmup }?.exerciseId
                ?: recent.firstOrNull()?.exerciseId
                ?: exercises.firstOrNull()?.id.orEmpty()
        }
        val exerciseRows = recent.filter { it.exerciseId == exerciseId }
        val exerciseName = exercises.firstOrNull { it.id == exerciseId }?.name
            ?: exerciseRows.firstOrNull()?.exerciseName.orEmpty()

        // Volume by muscle: work sets only, warmups excluded.
        val muscleOf = exercises.associate { it.id to it.muscleGroup }
        val tonnage = mutableMapOf<String, Double>()
        recent.filter { !it.isWarmup }.forEach { s ->
            val g = muscleOf[s.exerciseId] ?: return@forEach
            tonnage[g] = (tonnage[g] ?: 0.0) + s.weightKg * s.completedReps / 1000.0
        }
        val volumeByMuscle = tonnage.entries
            .sortedByDescending { it.value }
            .take(4)
            .map { (g, t) -> g to String.format(locale, "%.1f t", t) }

        if (recent.isEmpty()) {
            ProgressUiState(
                exercise = exerciseName,
                exerciseId = exerciseId,
                isEmpty = true,
                hasExerciseData = false,
                achievements = achievements,
            )
        } else {
            val exerciseSets = exerciseRows
                .map { WorkSetPoint(it.timestamp, it.weightKg, it.completedReps, it.isWarmup) }

            if (exerciseSets.none { !it.isWarmup }) {
                // Distinct state: history exists but this exercise has no work sets.
                ProgressUiState(
                    exercise = exerciseName,
                    exerciseId = exerciseId,
                    selectedMetric = metric.label,
                    volumeByMuscle = volumeByMuscle,
                    isEmpty = false,
                    hasExerciseData = false,
                    achievements = achievements,
                )
            } else {
                val series = dailyMetricSeries(exerciseSets, metric)
                val best = bestWorkSet(exerciseSets)
                val deltaPct = deltaPercent(series)
                val chartPoints = normalizeToChart(series)
                val deltaLabel = if (series.size < 2) "—" else String.format(locale, "%+.1f%%", deltaPct)
                val heroValue = when (metric) {
                    ProgressMetric.VOLUME -> String.format(locale, "%.1f t", series.last() / 1000.0)
                    ProgressMetric.REPS -> String.format(locale, "%d reps", series.last().toInt())
                    else -> String.format(locale, "%.1f KG", series.last())
                }
                val heroLabel = when (metric) {
                    ProgressMetric.STRENGTH -> "est. 1RM"
                    ProgressMetric.VOLUME -> "daily volume"
                    ProgressMetric.REPS -> "daily reps"
                    ProgressMetric.LOAD -> "top load"
                }
                ProgressUiState(
                    exercise = exerciseName.ifEmpty { "-" },
                    exerciseId = exerciseId,
                    heroValue = heroValue,
                    heroLabel = heroLabel,
                    delta = deltaLabel,
                    selectedMetric = metric.label,
                    points = chartPoints,
                    bestSet = best?.let { "${formatWeight(it.weightKg)} kg x ${it.reps}" } ?: "-",
                    volumeByMuscle = volumeByMuscle,
                    isEmpty = false,
                    hasExerciseData = true,
                    chartDescription = buildString {
                        append(metric.label)
                        append(" trend for ")
                        append(if (exerciseName.isEmpty()) "selected exercise" else exerciseName)
                        append(": ")
                        append(series.size)
                        append(if (series.size == 1) " day" else " days")
                        append(", latest ")
                        append(heroValue)
                        if (series.size >= 2) {
                            append(", change ")
                            append(deltaLabel)
                        }
                        append(".")
                    },
                    achievements = achievements,
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProgressUiState())

    fun selectMetric(m: String) {
        selectedMetric.value = m
    }

    private fun formatWeight(weightKg: Double): String =
        String.format(locale, if (weightKg % 1.0 == 0.0) "%.0f" else "%.1f", weightKg)
}

@Composable
fun ProgressRoute(viewModel: ProgressViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    ProgressScreen(state = state, onMetric = viewModel::selectMetric)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProgressScreen(state: ProgressUiState, onMetric: (String) -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("PROGRESS", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant)

        if (state.hasExerciseData) {
            ProgressHero(
                exercise = state.exercise,
                estimated1RM = state.heroValue,
                delta = state.delta,
                valueLabel = state.heroLabel,
            )
        } else {
            Text(
                when {
                    state.isEmpty -> "Log a workout to see real trends"
                    state.exercise.isNotEmpty() -> "No work sets logged for ${state.exercise} yet"
                    else -> "No work sets logged for this exercise yet"
                },
                style = RepForgeTypeRoles.BodySupport,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Restrained plot: one primary-colored line, no gridline noise.
        // Renders only real data — never a fabricated placeholder line.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RepForgeShapes.CardLarge)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(16.dp)
                .then(
                    if (state.chartDescription.isNotEmpty()) {
                        Modifier.semantics { contentDescription = state.chartDescription }
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (state.points.isEmpty()) {
                Text(
                    if (state.isEmpty) "No trends yet" else "No ${state.selectedMetric.lowercase()} data for this exercise yet",
                    style = RepForgeTypeRoles.BodySupport,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                val color = MaterialTheme.colorScheme.primary
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Density-aware dimensions: dp converted through the DrawScope Density,
                    // so stroke/dot size stay visually identical across screen densities.
                    val strokePx = 3.dp.toPx()
                    val dotRadiusPx = 5.dp.toPx()
                    val path = Path()
                    val stepX = size.width / (state.points.size - 1).coerceAtLeast(1)
                    state.points.forEachIndexed { i, v ->
                        val x = i * stepX
                        val y = size.height - (v * size.height * 0.8f) - size.height * 0.1f
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, color = color, style = Stroke(width = strokePx))
                    state.points.forEachIndexed { i, v ->
                        val x = i * stepX
                        val y = size.height - (v * size.height * 0.8f) - size.height * 0.1f
                        drawCircle(color = color, radius = dotRadiusPx, center = Offset(x, y))
                    }
                }
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ProgressMetric.entries.forEach { metric ->
                val selected = metric.label == state.selectedMetric
                FilterChip(
                    selected = metric.label == state.selectedMetric,
                    onClick = { onMetric(metric.label) },
                    label = { Text(metric.label) },
                    leadingIcon = {
                        Icon(
                            imageVector = metric.symbolRole.imageVector(selected),
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                        )
                    },
                )
            }
        }

        // Asymmetric editorial stats: ONE wide anchor tile + inline pill stats
        // (replaces the uniform card-per-stat dashboard).
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RepForgeShapes.CardLarge)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("VOLUME BY MUSCLE", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onPrimaryContainer)
            if (state.volumeByMuscle.isEmpty()) {
                Text("-", style = RepForgeTypeRoles.MetricMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            } else {
                state.volumeByMuscle.forEach { (muscle, tonnes) ->
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(muscle, style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(tonnes, style = RepForgeTypeRoles.MetricMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
        // FlowRow so stat pills wrap instead of clipping at large font sizes.
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatPill("BEST SET", state.bestSet)
            StatPill("DELTA", state.delta)
        }

        AchievementsMosaic(state.achievements)
    }
}

private val ProgressMetric.symbolRole: RepForgeSymbolRole
    get() = when (this) {
        ProgressMetric.STRENGTH -> RepForgeSymbols.ProgressStrength
        ProgressMetric.VOLUME -> RepForgeSymbols.ProgressVolume
        ProgressMetric.REPS -> RepForgeSymbols.ProgressReps
        ProgressMetric.LOAD -> RepForgeSymbols.ProgressLoad
    }

@Composable
private fun StatPill(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RepForgeShapes.Pill)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(label, style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurface)
    }
}

/**
 * Achievements mosaic (todo 13): varied shape tokens per tile — medium/large/
 * hero/full rotating — NOT a uniform card grid. Unlocked tiles pop in with the
 * expressive bounce spring.
 */
@Composable
private fun AchievementsMosaic(achievements: List<Achievement>) {
    if (achievements.isEmpty()) return
    val shapes = listOf(RepForgeShapes.CardMedium, RepForgeShapes.CardLarge, RepForgeShapes.Hero, RepForgeShapes.Pill)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("ACHIEVEMENTS", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            achievements.forEachIndexed { i, a ->
                MosaicTile(a, shapes[i % shapes.size])
            }
        }
    }
}

@Composable
private fun MosaicTile(a: Achievement, shape: androidx.compose.ui.graphics.Shape) {
    val scale = remember { Animatable(if (a.isUnlocked) 0.86f else 1f) }
    LaunchedEffect(a.isUnlocked) {
        if (a.isUnlocked) scale.animateTo(1f, animationSpec = RepForgeMotion.BouncySpring)
    }
    val bg = if (a.isUnlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest
    val fg = if (a.isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = Modifier
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .clip(shape)
            .background(bg)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(a.title, style = RepForgeTypeRoles.LabelExpressive, color = fg)
        Text(
            if (a.isUnlocked) "UNLOCKED" else "${(a.progress * 100).toInt()}%",
            style = RepForgeTypeRoles.BodySupport,
            color = fg,
        )
    }
}
