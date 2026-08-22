package com.repforge.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.repforge.core.designsystem.token.RepForgeShapes
import com.repforge.core.designsystem.token.RepForgeTypeRoles

/**
 * HeroMetric — disproportionately large, for TODAY hero and Progress 1RM.
 * Uses DisplayHero typography and Hero shape. Establishes VERY LOUD hierarchy.
 */
@Composable
fun HeroMetric(
    label: String,
    value: String,
    unit: String? = null,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    Column(
        modifier = modifier
            .clip(RepForgeShapes.Hero)
            .background(containerColor)
            .padding(horizontal = 26.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = RepForgeTypeRoles.LabelExpressive,
            color = contentColor.copy(alpha = 0.62f)
        )
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = value,
                style = RepForgeTypeRoles.MetricLarge,
                color = contentColor
            )
            if (unit != null) {
                Text(
                    text = unit,
                    style = RepForgeTypeRoles.LabelExpressive,
                    color = contentColor.copy(alpha = 0.72f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ExpressiveSplitHero(
    top: String,
    bottom: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground,
) {
    Column(modifier = modifier) {
        Text(text = top.uppercase(), style = RepForgeTypeRoles.DisplayHero, color = color.copy(alpha = 0.28f))
        Text(text = bottom.uppercase(), style = RepForgeTypeRoles.DisplayHero, color = color)
    }
}
