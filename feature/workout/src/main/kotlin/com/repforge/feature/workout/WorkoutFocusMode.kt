package com.repforge.feature.workout

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.repforge.core.designsystem.token.RepForgeTypeRoles

// Focus mode: when workout starts, chrome transforms — no analytics/nav, big exercise, 3D, previous, target, controls, rest. On finish, chrome returns.
@Composable
fun FocusChrome(enabled: Boolean, content: @Composable () -> Unit) {
    // In prod: animate nav bar to 0, window insets, predictive back
    Box(modifier = Modifier.fillMaxSize()) { content() }
}

@Composable
fun ChoreographyHint() {
    Text("Drag to reorder • long-press to superset • swipe to skip • tap equipment = occupied", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
}
