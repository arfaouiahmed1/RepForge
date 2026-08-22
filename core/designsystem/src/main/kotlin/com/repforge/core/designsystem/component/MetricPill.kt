package com.repforge.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.repforge.core.designsystem.token.RepForgeShapes
import com.repforge.core.designsystem.token.RepForgeTypeRoles

@Composable
fun MetricPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RepForgeShapes.MetricPill)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(label.uppercase(), style = RepForgeTypeRoles.LabelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f))
        Text(value, style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun ExpressiveWorkoutCard(
    title: String,
    subtitle: String,
    metric: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RepForgeShapes.CardLarge)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 22.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title.uppercase(), style = RepForgeTypeRoles.LabelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f))
        Text(subtitle, style = RepForgeTypeRoles.HeadlineLoud, color = MaterialTheme.colorScheme.onSurface)
        Text(metric, style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.76f))
    }
}

@Composable
fun ProgressHero(
    exercise: String,
    estimated1RM: String,
    delta: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RepForgeShapes.Hero)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 26.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(exercise.uppercase(), style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.68f))
        Text(estimated1RM, style = RepForgeTypeRoles.MetricLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text("estimated 1RM", style = RepForgeTypeRoles.LabelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.62f))
        Spacer(modifier = Modifier.height(6.dp))
        Text(delta, style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text("12 weeks", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.68f))
    }
}
