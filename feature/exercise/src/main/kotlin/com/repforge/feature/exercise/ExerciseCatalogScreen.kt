package com.repforge.feature.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.repforge.core.database.dao.ExerciseDao
import com.repforge.core.database.entity.ExerciseEntity
import com.repforge.core.designsystem.token.RepForgeShapes
import com.repforge.core.designsystem.token.RepForgeTypeRoles
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Singleton

enum class LocationFilter { ALL, HOME, GYM, HOME_GYM }
enum class EquipmentFilter { ALL, NO_EQUIPMENT, DUMBBELL, BARBELL, MACHINE_CABLE }

@HiltViewModel
class ExerciseCatalogViewModel @Inject constructor(
    private val exerciseDao: ExerciseDao,
) : ViewModel() {
    val allFlow: Flow<List<ExerciseEntity>> = exerciseDao.observeAll()
    val location = MutableStateFlow(LocationFilter.ALL)
    val muscle = MutableStateFlow<String?>(null)
    val equipment = MutableStateFlow(EquipmentFilter.ALL)
    val query = MutableStateFlow("")
}

private fun List<ExerciseEntity>.applyFilters(
    loc: LocationFilter,
    mus: String?,
    eq: EquipmentFilter,
    q: String,
): List<ExerciseEntity> = filter { e ->
    val locOk = when (loc) {
        LocationFilter.ALL -> true
        LocationFilter.HOME -> e.location == "HOME" || e.equipment == "BODYWEIGHT" || e.equipment == "BAND"
        LocationFilter.GYM -> e.location == "GYM"
        LocationFilter.HOME_GYM -> e.location == "BOTH" || e.equipment == "DUMBBELL"
    }
    val musOk = mus == null || e.muscleGroup == mus
    val eqOk = when (eq) {
        EquipmentFilter.ALL -> true
        EquipmentFilter.NO_EQUIPMENT -> e.equipment == "BODYWEIGHT"
        EquipmentFilter.DUMBBELL -> e.equipment == "DUMBBELL"
        EquipmentFilter.BARBELL -> e.equipment == "BARBELL"
        EquipmentFilter.MACHINE_CABLE -> e.equipment == "MACHINE" || e.equipment == "CABLE"
    }
    locOk && musOk && eqOk && (q.isBlank() || e.name.contains(q, ignoreCase = true))
}

@Composable
fun ExerciseCatalogRoute(
    onExerciseClick: (String) -> Unit,
    viewModel: ExerciseCatalogViewModel = hiltViewModel(),
) {
    val all by viewModel.allFlow.collectAsState(initial = emptyList())
    val loc by viewModel.location.collectAsState()
    val mus by viewModel.muscle.collectAsState()
    val eq by viewModel.equipment.collectAsState()
    val q by viewModel.query.collectAsState()

    val filtered = all.applyFilters(loc, mus, eq, q)

    ExerciseCatalogScreen(
        exercises = filtered,
        totalCount = all.size,
        location = loc,
        onLocation = { viewModel.location.value = it },
        muscle = mus,
        onMuscle = { viewModel.muscle.value = it },
        equipment = eq,
        onEquipment = { viewModel.equipment.value = it },
        query = q,
        onQuery = { viewModel.query.value = it },
        onClick = onExerciseClick,
    )
}

private enum class TileStyle { FILLED, OUTLINED, ELEVATED }

@Composable
fun ExerciseCatalogScreen(
    exercises: List<ExerciseEntity>,
    totalCount: Int,
    location: LocationFilter,
    onLocation: (LocationFilter) -> Unit,
    muscle: String?,
    onMuscle: (String?) -> Unit,
    equipment: EquipmentFilter,
    onEquipment: (EquipmentFilter) -> Unit,
    query: String,
    onQuery: (String) -> Unit,
    onClick: (String) -> Unit,
) {
    // Canonical large-screen constraint: content never exceeds 1040dp (skill layout rule).
    BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        val contentWidth = if (maxWidth > 1040.dp) 1040.dp else maxWidth

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 300.dp),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = contentWidth)
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Column {
                    Text("EXERCISES", style = RepForgeTypeRoles.DisplayHero, color = MaterialTheme.colorScheme.onBackground)
                    Text(
                        "$totalCount exercises \u00b7 Home vs Gym \u00b7 Muscle \u00b7 Equipment \u00b7 3D",
                        style = RepForgeTypeRoles.BodySupport,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = query,
                        onValueChange = onQuery,
                        label = { Text("Search bench, shoulder press\u2026") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            }
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                ChipSection(label = "LOCATION") {
                    LocationFilter.entries.forEach { f ->
                        TokenFilterChip(
                            label = when (f) {
                                LocationFilter.ALL -> "All"
                                LocationFilter.HOME -> "Home / no gym"
                                LocationFilter.GYM -> "Gym"
                                LocationFilter.HOME_GYM -> "Both / dumbbell"
                            },
                            selected = f == location,
                            onClick = { onLocation(f) },
                        )
                    }
                }
            }
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                ChipSection(label = "MUSCLE GROUP") {
                    TokenFilterChip(label = "All", selected = muscle == null, onClick = { onMuscle(null) })
                    listOf("CHEST", "BACK", "LEGS", "SHOULDERS", "ARMS", "CORE", "GLUTES", "QUADS", "HAMSTRINGS", "LATS", "TRICEPS", "BICEPS").forEach { m ->
                        TokenFilterChip(label = m, selected = m == muscle, onClick = { onMuscle(m) })
                    }
                }
            }
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                ChipSection(label = "EQUIPMENT") {
                    EquipmentFilter.entries.forEach { e ->
                        TokenFilterChip(
                            label = when (e) {
                                EquipmentFilter.ALL -> "All"
                                EquipmentFilter.NO_EQUIPMENT -> "No equipment"
                                EquipmentFilter.DUMBBELL -> "Dumbbell"
                                EquipmentFilter.BARBELL -> "Barbell"
                                EquipmentFilter.MACHINE_CABLE -> "Machine/Cable"
                            },
                            selected = e == equipment,
                            onClick = { onEquipment(e) },
                        )
                    }
                }
            }
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatPill("${exercises.count { it.location == "HOME" || it.equipment == "BODYWEIGHT" }} home")
                    StatPill("${exercises.count { it.location == "GYM" }} gym")
                    StatPill("${exercises.size} shown")
                }
            }
            items(exercises, key = { it.id }) { ex ->
                EditorialTile(exercise = ex, styleIndex = exercises.indexOf(ex), onClick = onClick)
            }
        }
    }
}

@Composable
private fun ChipSection(label: String, content: @Composable () -> Unit) {
    Column {
        Text(label, style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        Spacer(Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { content() } } }
    }
}

/**
 * Real MD3 FilterChip: tonal secondary-container selection + token shape-full pill.
 */
@Composable
private fun TokenFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        shape = RepForgeShapes.Pill,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    )
}

/**
 * Editorial tile (todo 12): three alternating MD3 card variants, 512px-WebP thumb SLOT
 * (no Filament hosts in list rows - zero-jank scroll contract), metadata via type roles.
 */
@Composable
private fun EditorialTile(exercise: ExerciseEntity, styleIndex: Int, onClick: (String) -> Unit) {
    val style = TileStyle.entries[styleIndex.mod(TileStyle.entries.size)]
    val shape = RepForgeShapes.CardMedium

    val tileModifier = Modifier
        .fillMaxWidth()
        .clip(shape)
        .clickable { onClick(exercise.id) }

    when (style) {
        TileStyle.FILLED -> Surface(
            modifier = tileModifier,
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) { TileContent(exercise) }
        TileStyle.OUTLINED -> Surface(
            modifier = tileModifier,
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) { TileContent(exercise) }
        TileStyle.ELEVATED -> Surface(
            modifier = tileModifier,
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 3.dp,
        ) { TileContent(exercise) }
    }
}

@Composable
private fun TileContent(exercise: ExerciseEntity) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ThumbSlot(muscleGroup = exercise.muscleGroup)
        Text(exercise.name, style = RepForgeTypeRoles.LabelExpressive.copy(fontWeight = FontWeight.W700), color = MaterialTheme.colorScheme.onSurface)
        Text(
            "${exercise.muscleGroup} \u00b7 ${exercise.equipment}",
            style = RepForgeTypeRoles.BodySupport,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            LocationBadge(exercise.location)
            Text("3D", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.primary)
        }
    }
}

/** Placeholder until 512px WebPs stream from R2 (todo 25/26); static, zero 3D hosts. */
@Composable
private fun ThumbSlot(muscleGroup: String) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RepForgeShapes.CardMedium)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            muscleGroup.take(1),
            style = RepForgeTypeRoles.MetricMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun LocationBadge(location: String) {
    val label = when (location) {
        "HOME" -> "HOME"
        "BOTH" -> "BOTH"
        else -> "GYM"
    }
    Text(label, style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun StatPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RepForgeShapes.Pill)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(text, style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
