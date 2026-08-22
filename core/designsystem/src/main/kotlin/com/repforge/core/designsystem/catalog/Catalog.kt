package com.repforge.core.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.repforge.core.designsystem.component.*
import com.repforge.core.designsystem.token.RepForgeShapes
import com.repforge.core.designsystem.token.RepForgeTypeRoles

/**
 * DesignSystemCatalog — portfolio review before building features.
 * Answers: Does this look like M3 Expressive or did we make the caffeine app again?
 * Shows: Typography, Shapes, Buttons, Metric displays, Workout controls, Timers, Nav, Charts, Motion, Light/dark, Font scaling.
 */
@Composable
fun DesignSystemCatalog() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text("RepForge — Design System Catalog", style = RepForgeTypeRoles.DisplayHero, color = MaterialTheme.colorScheme.onBackground)
        Text("One expressive focal interaction per screen. Hierarchy: VERY LOUD → LOUD → MEDIUM → QUIET", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Section("Typography — Google Sans Flex variable") {
            Text("PUSH DAY", style = RepForgeTypeRoles.DisplayHero, color = MaterialTheme.colorScheme.onBackground)
            Text("82.5 KG", style = RepForgeTypeRoles.MetricLarge, color = MaterialTheme.colorScheme.onBackground)
            Text("Last session: 80 kg × 8", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Section("Shapes — families not one radius") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(80.dp).clip(RepForgeShapes.Hero).background(MaterialTheme.colorScheme.primaryContainer))
                Box(Modifier.size(80.dp).clip(RepForgeShapes.PrimaryActionCircle).background(MaterialTheme.colorScheme.primary))
                Box(Modifier.size(80.dp).clip(RepForgeShapes.Metric).background(MaterialTheme.colorScheme.surfaceContainerHigh))
                Box(Modifier.size(80.dp).clip(RepForgeShapes.Media).background(MaterialTheme.colorScheme.secondaryContainer))
            }
            Text("Hero / Circle / Metric / Media — visibly contrast shape classes", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
        Section("HeroMetric") { HeroMetric(label = "Estimated 1RM", value = "94.3", unit = "KG") }
        Section("ConnectedNumberControl") {
            ConnectedNumberControl(value = "82.5", unit = "KG", onDecrement = {}, onIncrement = {})
        }
        Section("MorphingSetButton") { MorphingSetButton(onComplete = {}) }
        Section("RestTimer (hero, not card)") {
            RestTimer(totalSeconds = 90, remainingSeconds = 42, onAddTime = {}, onSkip = {}, onTimeUp = {}, nextExercise = "Bench Press", nextLoad = "82.5 kg × 8 • 78% success")
        }
        Section("ProgressHero") { ProgressHero(exercise = "BENCH PRESS", estimated1RM = "94.3 KG", delta = "↑ 7.2% • 12 weeks") }
        Section("WorkoutToolbar + SetRow") {
            WorkoutToolbar(title = "PUSH DAY", progress = "03 / 07")
            Spacer(Modifier.height(8.dp))
            SetRow(index = 1, load = "80", reps = "8", rir = "2", isCompleted = true)
            SetRow(index = 2, load = "82.5", reps = "8", rir = "1", isCompleted = false)
        }
    }
}

@Composable private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title.uppercase(), style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        content()
    }
}
