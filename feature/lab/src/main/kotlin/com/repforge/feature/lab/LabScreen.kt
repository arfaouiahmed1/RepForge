package com.repforge.feature.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.repforge.core.designsystem.token.RepForgeShapes
import com.repforge.core.designsystem.token.RepForgeTypeRoles

@Composable
fun LabRoute(
    onNavigateFormLab: () -> Unit,
    onNavigateModel: () -> Unit,
) {
    LabScreen(onNavigateFormLab = onNavigateFormLab, onNavigateModel = onNavigateModel)
}

@Composable
fun LabScreen(
    onNavigateFormLab: () -> Unit,
    onNavigateModel: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("TRAINING LAB", style = RepForgeTypeRoles.DisplayHero, color = MaterialTheme.colorScheme.onBackground)
        Text("BETA — experimental, keeps nerdy ML features out of core workout", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant)

        LabCard(title = "Form Lab", subtitle = "CameraX + ML Kit Pose • squat depth, tempo, ROM", onClick = onNavigateFormLab)
        LabCard(title = "Adaptive progression", subtitle = "Model insights • 79% target success explains why", onClick = onNavigateModel)
        LabCard(title = "Training experiments", subtitle = "A/B templates • volume vs intensity", onClick = {})
    }
}

@Composable
private fun LabCard(title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RepForgeShapes.CardLarge).background(MaterialTheme.colorScheme.surfaceVariant).clickable(onClick = onClick).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(title.uppercase(), style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        Text(title, style = RepForgeTypeRoles.HeadlineLoud, color = MaterialTheme.colorScheme.onSurface)
        Text(subtitle, style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
