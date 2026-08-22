package com.repforge.feature.paywall

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.repforge.core.designsystem.component.GiantStartShape
import com.repforge.core.designsystem.token.RepForgeShapes
import com.repforge.core.designsystem.token.RepForgeTypeRoles

@Composable
fun PaywallRoute(onSubscribe: () -> Unit, onRestore: () -> Unit) {
    PaywallScreen(onSubscribe = onSubscribe, onRestore = onRestore)
}

@Composable
fun PaywallScreen(onSubscribe: () -> Unit, onRestore: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("REP\nFORGE PRO", style = RepForgeTypeRoles.DisplayHeroLarge, color = MaterialTheme.colorScheme.onBackground)
        Text("Actual workout logging is unlimited free. Pro is for progression intelligence.", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("FREE", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            Bullet("Unlimited workout logging")
            Bullet("History • PRs • Basic charts")
            Bullet("Health Connect • Watch logging")
        }
        Column(
            modifier = Modifier.clip(RepForgeShapes.CardLarge).background(MaterialTheme.colorScheme.primaryContainer).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("PRO", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
            Bullet("Adaptive progression engine (on-device)")
            Bullet("Advanced analytics • Form Lab • Readiness")
            Bullet("Cross-device sync • Export • Experimental features")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.clip(RepForgeShapes.Pill).background(MaterialTheme.colorScheme.primary).padding(horizontal = 18.dp, vertical = 10.dp)) {
                    Text("$3.99 / mo  or  $29.99 / yr", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }

        Spacer(Modifier.weight(1f))
        GiantStartShape(onClick = onSubscribe, label = "START TRIAL")
        Text("Restore purchases • Never trust SharedPreferences(\"isPro\") — verified via Play Developer API + RTDN", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
    }
}

@Composable private fun Bullet(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("•", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text, style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurface)
    }
}
