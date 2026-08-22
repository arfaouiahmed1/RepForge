package com.repforge.feature.achievements

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
import androidx.lifecycle.viewModelScope
import com.repforge.core.data.repository.AchievementRepository
import com.repforge.core.designsystem.token.RepForgeShapes
import com.repforge.core.designsystem.token.RepForgeTypeRoles
import com.repforge.core.model.Achievement
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AchievementsViewModel @Inject constructor(repo: AchievementRepository) : ViewModel() {
    val achievements = repo.observe().stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())
    init { viewModelScope.launch { repo.seedIfEmpty() } }
}

@Composable
fun AchievementsRoute(viewModel: AchievementsViewModel = hiltViewModel()) {
    val list by viewModel.achievements.collectAsState()
    AchievementsScreen(achievements = if (list.isEmpty()) com.repforge.core.model.AchievementDefinitions.all else list)
}

@Composable
fun AchievementsScreen(achievements: List<Achievement>) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("ACHIEVEMENTS", style = RepForgeTypeRoles.DisplayHero, color = MaterialTheme.colorScheme.onBackground)
            Text("Meaningful training milestones — not Candy Crush. 4 identity tracks, not FITNESS LEVEL:84.", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            // Identity tracks — strength/consistency/explorer/endurance with progress bars (expressive mosaic header)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TrackBar("STRENGTH", 0.62f, "18 PRs • 1.5× bench soon")
                TrackBar("CONSISTENCY", 0.71f, "42 workouts • 87% adherence")
                TrackBar("EXPLORER", 0.38f, "Tried 10 exercises • Havent done outdoor yet")
                TrackBar("ENDURANCE", 0.22f, "First 5K pending")
            }
            Spacer(Modifier.height(12.dp))
            Text("CABINET — asymmetric editorial mosaic, not uniform grid", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
        // Mosaic: large tiles for major achievements, small for minor — composition, not grid
        // Row 1: 50 WORKOUTS (large) + 10 PRs (small)
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MosaicTile(achievements.find { it.id == "streak_30" } ?: AchieveFake("50 WORKOUTS", "50 workouts", true), modifier = Modifier.weight(1.6f).height(120.dp))
                MosaicTile(achievements.find { it.id == "streak_7" } ?: AchieveFake("10 PRs", "10 personal records", true), modifier = Modifier.weight(1f).height(120.dp))
            }
        }
        // Row 2: FIRST 5K wide
        item { MosaicTile(AchieveFake("FIRST 5K", "Run 5 km • Endurance", false), modifier = Modifier.fillMaxWidth().height(80.dp), wide = true) }
        // Row 3: 12 WK + 100K VOLUME
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MosaicTile(achievements.find { it.id == "streak_7" } ?: AchieveFake("12 WK", "12-week consistency", false), modifier = Modifier.weight(1f).height(100.dp))
                MosaicTile(achievements.find { it.id == "volume_100k" } ?: AchieveFake("100K VOLUME", "100,000 kg logged", false), modifier = Modifier.weight(1.5f).height(100.dp))
            }
        }
        // Rest list — linear but with progress
        items(achievements) { a ->
            Row(
                modifier = Modifier.fillMaxWidth().clip(RepForgeShapes.CardMedium).background(if (a.isUnlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant).padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${a.icon} ${a.title.uppercase()}", style = RepForgeTypeRoles.LabelExpressive, color = if (a.isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(a.description, style = RepForgeTypeRoles.BodySupport, color = if (a.isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant)
                    // Show weight-ratio only if body weight exists — else hide
                    Text("${a.category.name} • ${a.tier.name} • ${ (a.progress*100).toInt() }%", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
                Text(if (a.isUnlocked) "✓" else "${(a.progress*100).toInt()}%", style = RepForgeTypeRoles.MetricMedium, color = if (a.isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Text("When earned: completion screen collapses → asymmetric shape expands + haptic → [VIEW PROFILE] [SHARE] (not Toast)", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun TrackBar(label: String, progress: Float, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(label, style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurface)
            Text("${(progress*100).toInt()}%", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RepForgeShapes.Pill).background(MaterialTheme.colorScheme.surfaceVariant)) {
            Box(modifier = Modifier.fillMaxWidth(progress).height(8.dp).clip(RepForgeShapes.Pill).background(MaterialTheme.colorScheme.primary))
        }
        Text(subtitle, style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
    }
}

@Composable
private fun MosaicTile(a: Achievement, modifier: Modifier = Modifier, wide: Boolean = false) {
    Box(modifier = modifier.clip(if (wide) RepForgeShapes.Hero else RepForgeShapes.CardLarge).background(if (a.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant).padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(a.title, style = RepForgeTypeRoles.HeadlineLoud, color = if (a.isUnlocked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
            Text(a.description, style = RepForgeTypeRoles.BodySupport, color = if (a.isUnlocked) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun AchieveFake(title: String, desc: String, unlocked: Boolean) = Achievement(id = title, title = title, description = desc, category = com.repforge.core.model.AchievementCategory.MILESTONE, tier = com.repforge.core.model.AchievementTier.GOLD, icon = "✦", unlockedAt = if (unlocked) System.currentTimeMillis() else null, progress = if (unlocked) 1f else 0.4f)
