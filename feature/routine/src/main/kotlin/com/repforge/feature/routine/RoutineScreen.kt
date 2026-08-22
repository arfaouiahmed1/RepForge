package com.repforge.feature.routine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.repforge.core.designsystem.token.RepForgeShapes
import com.repforge.core.designsystem.token.RepForgeTypeRoles
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class RoutineUiState(
    val weekLabel: String = "AUG 17 – 23",
    val days: List<String> = listOf("M", "T", "W", "T", "F", "S", "S"),
    val dots: List<Boolean> = listOf(true, false, true, false, true, false, false),
    val exercises: List<String> = listOf("Bench Press             4 × 8", "Overhead Press          3 × 8", "Incline DB Press        3 × 10", "Lateral Raise           4 × 12", "Triceps Pushdown        3 × 12")
)

@HiltViewModel
class RoutineViewModel @Inject constructor() : ViewModel() {}

@Composable
fun RoutineRoute(viewModel: RoutineViewModel = hiltViewModel()) {
    RoutineScreen()
}

@Composable
fun RoutineScreen() {
    val state = RoutineUiState()
    LazyColumn(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text(state.weekLabel, style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                state.days.forEachIndexed { i, d ->
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(d, style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (state.dots[i]) Box(modifier = Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(MaterialTheme.colorScheme.primary))
                        else Spacer(Modifier.size(8.dp))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("PUSH", "PULL", "LEGS").forEach { label ->
                    Box(modifier = Modifier.clip(RepForgeShapes.Pill).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 14.dp, vertical = 8.dp)) {
                        Text(label, style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item {
            Text("PUSH DAY", style = RepForgeTypeRoles.HeadlineLoud, color = MaterialTheme.colorScheme.onBackground)
        }
        items(state.exercises) { ex ->
            Box(modifier = Modifier.fillMaxWidth().clip(RepForgeShapes.CardMedium).background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp)) {
                Text(ex, style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
