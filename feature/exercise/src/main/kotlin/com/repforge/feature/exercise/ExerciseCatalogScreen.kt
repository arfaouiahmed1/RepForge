package com.repforge.feature.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlinx.coroutines.flow.combine

enum class LocationFilter { ALL, HOME, GYM, HOME_GYM }
enum class EquipmentFilter { ALL, NO_EQUIPMENT, DUMBBELL, BARBELL, MACHINE_CABLE }

@HiltViewModel
class ExerciseCatalogViewModel @Inject constructor(
    private val exerciseDao: ExerciseDao,
) : ViewModel() {
    val allFlow: Flow<List<ExerciseEntity>> = exerciseDao.observeAll()
    val location = MutableStateFlow(LocationFilter.ALL)
    val muscle = MutableStateFlow<String?>(null) // null = all, else "CHEST"
    val equipment = MutableStateFlow(EquipmentFilter.ALL)
    val query = MutableStateFlow("")
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

    // seeded fallback while Room empty (before seeder runs) — show 60 seed names
    val fallback = remember { com.repforge.core.database.seed.ExerciseSeed.exercises.map { it.name to it.id } }

    val filtered = remember(all, loc, mus, eq, q) {
        val source = if (all.isNotEmpty()) all.map { it.name to it.id to (it.location to it.equipment to it.muscleGroup) } else fallback.map { (n, id) -> Triple(n, id, Triple("HOME", "BODYWEIGHT", "CHEST")) }
        // Actually filter using entities when available
        var list = all.ifEmpty { com.repforge.core.database.seed.ExerciseSeed.exercises }
        list = list.filter { e ->
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
                EquipmentFilter.MACHINE_CABLE -> e.equipment in listOf("MACHINE", "CABLE")
            }
            val qOk = q.isBlank() || e.name.contains(q, ignoreCase = true)
            locOk && musOk && eqOk && qOk
        }
        list
    }

    ExerciseCatalogScreen(
        exercises = filtered,
        location = loc,
        onLocation = { viewModel.location.value = it },
        muscle = mus,
        onMuscle = { viewModel.muscle.value = it },
        equipment = eq,
        onEquipment = { viewModel.equipment.value = it },
        query = q,
        onQuery = { viewModel.query.value = it },
        onClick = onExerciseClick
    )
}

@Composable
fun ExerciseCatalogScreen(
    exercises: List<ExerciseEntity>,
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
    LazyColumn(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("EXERCISES", style = RepForgeTypeRoles.DisplayHero, color = MaterialTheme.colorScheme.onBackground)
            Text("${exercises.size} exercises • Home vs Gym • Muscle • Equipment • 3D", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = query, onValueChange = onQuery, label = { Text("Search bench, shoulder press…") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        }
        item {
            Text("LOCATION", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(LocationFilter.entries) { f ->
                    FilterChip(label = when (f) { LocationFilter.ALL -> "All"; LocationFilter.HOME -> "Home • no gym"; LocationFilter.GYM -> "Gym"; LocationFilter.HOME_GYM -> "Both / Dumbbell" }, selected = f == location, onClick = { onLocation(f) })
                }
            }
        }
        item {
            Text("MUSCLE GROUP", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FilterChip(label = "All", selected = muscle == null, onClick = { onMuscle(null) }) }
                items(listOf("CHEST", "BACK", "LEGS", "SHOULDERS", "ARMS", "CORE", "GLUTES", "QUADS", "HAMSTRINGS", "LATS", "TRICEPS", "BICEPS")) { m ->
                    FilterChip(label = m, selected = m == muscle, onClick = { onMuscle(m) })
                }
            }
        }
        item {
            Text("EQUIPMENT", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(EquipmentFilter.entries) { e ->
                    FilterChip(label = when (e) { EquipmentFilter.ALL -> "All"; EquipmentFilter.NO_EQUIPMENT -> "No equipment"; EquipmentFilter.DUMBBELL -> "Dumbbell"; EquipmentFilter.BARBELL -> "Barbell"; EquipmentFilter.MACHINE_CABLE -> "Machine/Cable" }, selected = e == equipment, onClick = { onEquipment(e) })
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatPill("${exercises.count { it.location == "HOME" || it.equipment == "BODYWEIGHT" }} home")
                StatPill("${exercises.count { it.location == "GYM" }} gym")
                StatPill("${exercises.count { it.equipment == "BODYWEIGHT" }} no equipment")
            }
        }
        items(exercises) { ex ->
            Row(
                modifier = Modifier.fillMaxWidth().clip(RepForgeShapes.CardMedium).background(MaterialTheme.colorScheme.surfaceVariant).clickable { onClick(ex.id) }.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(ex.name, style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurface)
                    Text("${ex.muscleGroup} • ${ex.equipment} • ${ex.location} • 3D: ${ex.glbAsset ?: "—"}", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(if (ex.location == "HOME") "HOME" else if (ex.location == "BOTH") "BOTH" else "GYM", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.clip(RepForgeShapes.Pill).background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant).clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp)) {
        Text(label, style = RepForgeTypeRoles.LabelExpressive, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StatPill(text: String) {
    Box(modifier = Modifier.clip(RepForgeShapes.Pill).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 12.dp, vertical = 6.dp)) {
        Text(text, style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
