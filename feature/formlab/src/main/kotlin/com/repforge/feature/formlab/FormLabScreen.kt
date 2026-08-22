package com.repforge.feature.formlab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.repforge.core.designsystem.token.RepForgeShapes
import com.repforge.core.designsystem.token.RepForgeTypeRoles

/**
 * Form Lab — CameraX + ML Kit Pose Detection in STREAM mode (tracks prominent person, smooths).
 * For squat: standing → descending → bottom → ascending → standing = 1 rep.
 * Keep BETA visible, never medical claims. Good: "14% lower ROM last 3 reps" Bad: "knee at risk"
 *
 * Metrics: rep-count accuracy, phase F1, ROM error, inference latency, FPS
 */
@Composable
fun FormLabRoute() { FormLabScreen() }

@Composable
fun FormLabScreen() {
    var phase by remember { mutableStateOf("standing") }
    var rep by remember { mutableStateOf(7) }
    var depth by remember { mutableStateOf(94) }
    var tempo by remember { mutableStateOf("2.1 / 0.8 / 1.3 sec") }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("FORM LAB", style = RepForgeTypeRoles.DisplayHero, color = MaterialTheme.colorScheme.onBackground)
            Box(modifier = Modifier.clip(RepForgeShapes.Pill).background(MaterialTheme.colorScheme.errorContainer).padding(horizontal = 10.dp, vertical = 4.dp)) {
                Text("BETA", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
        // Camera placeholder — in prod use androidx.camera.lifecycle.ProcessCameraProvider + MlKit PoseDetector STREAM
        Box(
            modifier = Modifier.fillMaxWidth().height(360.dp).clip(RepForgeShapes.CardLarge).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("CAMERA", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                Text("skeleton overlay", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("REP $rep — $phase", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.primary)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricBox(label = "Depth", value = "${depth}°", modifier = Modifier.weight(1f))
            MetricBox(label = "Tempo", value = tempo, modifier = Modifier.weight(1f))
        }
        Box(modifier = Modifier.fillMaxWidth().clip(RepForgeShapes.Metric).background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("ROM consistency", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                Text("92%", style = RepForgeTypeRoles.MetricMedium, color = MaterialTheme.colorScheme.onSurface)
                Text("Your final three reps showed 14% lower measured range of motion.", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text("Good: \"14% lower ROM\"  •  Bad: \"knee at risk\" — never medical diagnosis.", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
    }
}

@Composable
private fun MetricBox(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clip(RepForgeShapes.Metric).background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label.uppercase(), style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        Text(value, style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurface)
    }
}
