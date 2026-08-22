package com.repforge.feature.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.repforge.core.designsystem.component.GiantStartShape
import com.repforge.core.designsystem.token.RepForgeTypeRoles

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text("REP\nFORGE", style = RepForgeTypeRoles.DisplayHeroLarge, color = MaterialTheme.colorScheme.onBackground)
        Text("Log your workout incredibly fast. See whether you are actually getting stronger. Let the app learn what load you can handle next.", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        GiantStartShape(onClick = onFinish, label = "START TRAINING")
        Text("No account required to start. Create one later to sync.", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
    }
}
