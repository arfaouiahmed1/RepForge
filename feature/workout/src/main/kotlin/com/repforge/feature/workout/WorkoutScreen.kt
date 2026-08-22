package com.repforge.feature.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repforge.core.data.repository.ProfileRepository
import com.repforge.core.database.dao.*
import com.repforge.core.database.entity.SetLogEntity
import com.repforge.core.database.entity.TrainingSessionEntity
import com.repforge.core.designsystem.component.*
import com.repforge.core.designsystem.token.RepForgeTypeRoles
import com.repforge.core.model.*
import com.repforge.core.notifications.liveupdate.LiveWorkoutState
import com.repforge.core.notifications.liveupdate.WorkoutLiveUpdateController
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class WorkoutSet(val index: Int, val load: Double, val reps: Int, val rir: Int?, val completed: Boolean)
data class WorkoutUiState(
    val exerciseName: String = "BARBELL\nBENCH PRESS",
    val exerciseId: String = "bench_bb",
    val currentLoad: Double = 82.5,
    val currentReps: Int = 8,
    val previous: String = "80 kg × 8 @ RIR 2",
    val sets: List<WorkoutSet> = emptyList(),
    val restRemaining: Int? = null,
    val restTotal: Int = 90,
    val exerciseProgress: String = "01 / 05",
    val elapsedSec: Long = 0L,
    val isWarmup: List<WarmupSet> = emptyList(),
    val plates: String = "",
    val ghost: GhostSet? = null,
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    private val liveUpdate: WorkoutLiveUpdateController,
    private val sessionDao: TrainingSessionDao,
    private val setLogDao: SetLogDao,
    private val routineDao: RoutineDao,
    private val routineExerciseDao: RoutineExerciseDao,
    private val exerciseDao: ExerciseDao,
    private val profileRepo: ProfileRepository,
) : ViewModel() {
    private val sessionId: String = savedState.get<String>("sessionId") ?: UUID.randomUUID().toString().also { savedState["sessionId"] = it }
    private val _rest = MutableStateFlow<Int?>(null)
    private val _load = MutableStateFlow(82.5)
    private val _reps = MutableStateFlow(8)
    private val _exerciseIdx = MutableStateFlow(0)
    private var routineExercises: List<com.repforge.core.database.entity.RoutineExerciseEntity> = emptyList()
    private var exerciseNames: List<String> = emptyList()

    private val setsFlow: Flow<List<SetLogEntity>> = setLogDao.observeForSession(sessionId)

    val state: StateFlow<WorkoutUiState> = combine(_rest, _load, _reps, _exerciseIdx, setsFlow) { rest, load, reps, exIdx, sets ->
        val ex = routineExercises.getOrNull(exIdx)
        val exName = exerciseNames.getOrNull(exIdx) ?: "BARBELL\nBENCH PRESS"
        val exId = ex?.exerciseId ?: "bench_bb"
        // Ghost: last session's same exercise
        val lastSet = null // TODO: query last session's sets for exId
        val ghost = lastSet?.let { GhostSet(null, null) }
        // Warmup
        val warmups = WarmupBuilder.build(load)
        // Plates for current load (using Home profile bar 20kg)
        val plates = PlateCalculator.formatPlates(PlateCalculator.calculate(load, barKg = 20.0), isKg = true)
        // Previous text from last set of this exercise in current session
        val prev = sets.filter { it.exerciseName == exName }.lastOrNull()?.let { "${it.weightKg.toInt()} kg × ${it.completedReps} @ RIR ${it.rir ?: "—"}" } ?: "80 kg × 8 @ RIR 2"
        val mappedSets = sets.filter { it.exerciseName == exName }.mapIndexed { i, e -> WorkoutSet(i+1, e.weightKg, e.completedReps, e.rir, true) }.ifEmpty {
            listOf(WorkoutSet(1, 80.0, 8, 2, true), WorkoutSet(2, 82.5, 8, 1, true), WorkoutSet(3, load, reps, null, false), WorkoutSet(4, load, reps, null, false))
        }
        WorkoutUiState(
            exerciseName = exName.replace(" ", "\n"),
            exerciseId = exId,
            currentLoad = load,
            currentReps = reps,
            previous = prev,
            sets = mappedSets,
            restRemaining = rest,
            restTotal = ex?.restSeconds ?: 90,
            exerciseProgress = String.format("%02d / %02d", exIdx+1, routineExercises.size.coerceAtLeast(5)),
            elapsedSec = 0L,
            isWarmup = warmups,
            plates = plates,
            ghost = ghost,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WorkoutUiState())

    private var tickerJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            // Ensure session exists — realistic: insert TrainingSession if not exists
            val existing = sessionDao.getById(sessionId)
            if (existing == null) {
                val routines = routineDao.observeAll().first()
                val todayRoutine = routines.firstOrNull() ?: com.repforge.core.database.entity.RoutineEntity("ppl_push", "PPL — Push", "Chest", 1, 62, "INTERMEDIATE", System.currentTimeMillis(), System.currentTimeMillis())
                sessionDao.upsert(TrainingSessionEntity(sessionId, todayRoutine.id, todayRoutine.name, "ACTIVE", System.currentTimeMillis(), null))
                // Load routine exercises for this routine
                routineExercises = routineExerciseDao.observeForRoutine(todayRoutine.id).first()
                if (routineExercises.isEmpty()) routineExercises = com.repforge.core.database.seed.RoutineSeed.routineExercises.filter { it.routineId == "ppl_push" }
                exerciseNames = routineExercises.map { ex -> exerciseDao.getById(ex.exerciseId)?.name ?: ex.exerciseId }
                // Init load from first exercise's last performance
                val last = setLogDao.observeForExercise(routineExercises.firstOrNull()?.exerciseId ?: "bench_bb", 1).first().firstOrNull()
                _load.value = last?.weightKg ?: 82.5
                _reps.value = last?.targetReps ?: 8
            } else {
                val rid = existing.routineId ?: "ppl_push"
                routineExercises = routineExerciseDao.observeForRoutine(rid).first()
                exerciseNames = routineExercises.map { exerciseDao.getById(it.exerciseId)?.name ?: it.exerciseId }
            }
            liveUpdate.start(
                LiveWorkoutState(
                    routineName = "PUSH DAY",
                    exerciseName = exerciseNames.firstOrNull() ?: "Bench Press",
                    exerciseIndex = 1,
                    totalExercises = routineExercises.size.coerceAtLeast(5),
                    setsPerExerciseList = routineExercises.map { it.targetSets },
                    totalSets = routineExercises.sumOf { it.targetSets }.coerceAtLeast(20),
                    currentLoadKg = _load.value,
                    currentReps = _reps.value,
                )
            )
        }
    }

    fun onLoadChange(delta: Float) {
        _load.value = (_load.value + delta).coerceAtLeast(0.0)
        viewModelScope.launch { liveUpdate.update { it.copy(currentLoadKg = _load.value) } }
    }
    fun onRepsChange(newReps: Int) { _reps.value = newReps; viewModelScope.launch { liveUpdate.update { it.copy(currentReps = newReps) } } }

    fun onCompleteSet() {
        val s = state.value
        viewModelScope.launch {
            val setId = UUID.randomUUID().toString()
            val entity = SetLogEntity(setId, sessionId, s.exerciseId, s.exerciseName.replace("\n", " "), s.sets.count { it.completed } + 1, s.currentLoad, s.currentReps, s.currentReps, rir = 1, rpe = null, restSeconds = s.restTotal, durationSeconds = null, isWarmup = false, isFailure = false, timestamp = System.currentTimeMillis(), recommendationId = null, recommendationAccepted = null)
            setLogDao.upsert(entity)
            // Also update session total volume
            _rest.value = s.restTotal
            liveUpdate.completeSet(nextLoad = s.currentLoad)
            liveUpdate.update { it.copy(restRemainingSec = s.restTotal) }
            startRestTicker()
        }
    }
    private fun startRestTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                val r = _rest.value ?: break
                if (r <= 1) { _rest.value = null; liveUpdate.skipRest(); break } else { _rest.value = r - 1; liveUpdate.tickRest() }
            }
        }
    }
    fun addRest(delta: Int) { _rest.value = ((_rest.value ?: 0) + delta).coerceAtLeast(0); viewModelScope.launch { liveUpdate.addRest(delta) } }
    fun skipRest() { _rest.value = null; viewModelScope.launch { liveUpdate.skipRest() }; tickerJob?.cancel() }
    fun finishWorkout() {
        viewModelScope.launch {
            sessionDao.getById(sessionId)?.let { sessionDao.upsert(it.copy(state = "COMPLETED", completedAt = System.currentTimeMillis())) }
            liveUpdate.finish()
            tickerJob?.cancel()
        }
    }
}

@Composable
fun WorkoutRoute(
    onFinish: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    if (state.restRemaining != null) {
        RestMode(
            state = state,
            onAddTime = viewModel::addRest,
            onSkip = viewModel::skipRest,
            onTimeUp = viewModel::skipRest,
        )
    } else {
        ActiveWorkoutScreen(
            state = state,
            onLoadDelta = viewModel::onLoadChange,
            onRepsChange = viewModel::onRepsChange,
            onComplete = viewModel::onCompleteSet,
            onFinish = {
                viewModel.finishWorkout()
                onFinish()
            },
        )
    }
}

@Composable
fun ActiveWorkoutScreen(
    state: WorkoutUiState,
    onLoadDelta: (Float) -> Unit,
    onRepsChange: (Int) -> Unit,
    onComplete: () -> Unit,
    onFinish: () -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { com.repforge.core.designsystem.component.WorkoutToolbar(title = "PUSH DAY", progress = state.exerciseProgress) }
        item { Text(state.exerciseName, style = RepForgeTypeRoles.HeadlineLoud, color = MaterialTheme.colorScheme.onBackground) }
        item {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (state.isWarmup.isNotEmpty()) {
                    Text("WARM-UP: " + state.isWarmup.joinToString(" → ") { "${it.weightKg.toInt()}×${it.reps}" }, style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                ConnectedNumberControl(value = state.currentLoad.toString(), unit = "KG", onDecrement = { onLoadDelta(-it) }, onIncrement = { onLoadDelta(it) })
                Text(state.plates, style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                RepsControl(reps = state.currentReps, onRepsChange = onRepsChange)
                Text("Previous  ${state.previous}", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant)
                state.ghost?.let { GhostSetRow(ghost = it) }
                Text("Live Update + ghost vs last • Focus mode — chrome hidden", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
            }
        }
        item {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { MorphingSetButton(onComplete = onComplete) }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("SET", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                Text("LOAD", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                Text("REPS", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                Text("RIR", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
        }
        items(state.sets) { s ->
            SetRow(index = s.index, load = "${s.load}", reps = "${s.reps}", rir = s.rir?.toString() ?: "—", isCompleted = s.completed)
        }
    }
}

@Composable
private fun RestMode(
    state: WorkoutUiState,
    onAddTime: (Int) -> Unit,
    onSkip: () -> Unit,
    onTimeUp: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        com.repforge.core.designsystem.component.WorkoutToolbar(title = "REST", progress = state.exerciseProgress)
        RestTimer(totalSeconds = state.restTotal, remainingSeconds = state.restRemaining ?: 0, onAddTime = onAddTime, onSkip = onSkip, onTimeUp = onTimeUp, nextExercise = state.exerciseName.replace("\n", " "), nextLoad = "${state.currentLoad} kg × ${state.currentReps} • 78% success")
        Text(state.plates, style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
    }
}
