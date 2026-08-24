package com.repforge.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repforge.core.data.repository.AchievementRepository
import com.repforge.core.data.repository.ProfileRepository
import com.repforge.core.datastore.PreferencesDataSource
import com.repforge.core.designsystem.token.RepForgeShapes
import com.repforge.core.designsystem.token.RepForgeTypeRoles
import com.repforge.core.model.ThemeMode
import com.repforge.core.model.UserProfile
import com.repforge.core.notifications.ReminderNotifier
import com.repforge.core.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val profileRepo: ProfileRepository,
    val achievementRepo: AchievementRepository,
    private val scheduler: ReminderScheduler,
    private val notifier: ReminderNotifier,
    private val preferencesDataSource: PreferencesDataSource,
) : ViewModel() {
    val profile = profileRepo.observeProfile().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val body = profileRepo.observeLatestBody().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val achievements = achievementRepo.observe().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val themeMode = preferencesDataSource.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)
    val dynamicColor = preferencesDataSource.dynamicColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferencesDataSource.setThemeMode(mode) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { preferencesDataSource.setDynamicColor(enabled) }
    }

    fun saveProfile(displayName: String, heightCm: Double?, weightKg: Double?) {
        viewModelScope.launch {
            val current = profileRepo.getProfile() ?: UserProfile(id = "user1", createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis(), heightCm = heightCm, weightKg = weightKg)
            val updated = current.copy(displayName = displayName.ifBlank { current.displayName }, heightCm = heightCm ?: current.heightCm, weightKg = weightKg ?: current.weightKg, updatedAt = System.currentTimeMillis())
            profileRepo.upsertProfile(updated)
            if (weightKg != null || heightCm != null) profileRepo.logBodyMetric(weightKg, heightCm)
        }
    }

    fun enableDaily(hour: Int = 18) { scheduler.scheduleDailyReminder(hour, 0) }
    fun enableBodyWeekly() { scheduler.scheduleBodyReminderWeekly() }
    fun enableWeeklySummary() { scheduler.scheduleWeeklySummary() }
    fun testRest() { notifier.notifyRestComplete("Bench Press") }
    fun testReminder() { notifier.notifyWorkoutReminder() }
    fun testAchievement() { notifier.notifyAchievement("New PR", "Bench Press 85 kg × 7 — est 1RM 102 kg") }
    fun cancelAllNotifications() { scheduler.cancelAll() }
}

@Composable
fun SettingsRoute(
    onNavigatePaywall: () -> Unit = {},
    onNavigateCatalog: () -> Unit = {},
    onNavigateAchievements: () -> Unit = {},
    onNavigateExercise: (String) -> Unit = {},
    onNavigateExercises: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val profile by viewModel.profile.collectAsState()
    val body by viewModel.body.collectAsState()
    val ach by viewModel.achievements.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val dynamicColor by viewModel.dynamicColor.collectAsState()
    SettingsScreen(
        profile = profile, bodyBmi = body?.bmi ?: profile?.bmi(), achievementsUnlocked = ach.count { it.isUnlocked },
        achievementTitles = ach.filter { it.isUnlocked }.map { it.title },
        themeMode = themeMode,
        dynamicColor = dynamicColor,
        onThemeMode = viewModel::setThemeMode,
        onDynamicColor = viewModel::setDynamicColor,
        onSave = viewModel::saveProfile,
        onTestRest = viewModel::testRest, onTestReminder = viewModel::testReminder, onTestAchievement = viewModel::testAchievement,
        onEnableDaily = viewModel::enableDaily, onEnableBody = viewModel::enableBodyWeekly, onEnableWeekly = viewModel::enableWeeklySummary,
        onNavigatePaywall = onNavigatePaywall, onNavigateCatalog = onNavigateCatalog, onNavigateAchievements = onNavigateAchievements, onNavigateExercise = onNavigateExercise, onNavigateExercises = onNavigateExercises
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    profile: UserProfile?,
    bodyBmi: Double?,
    achievementsUnlocked: Int,
    achievementTitles: List<String>,
    themeMode: ThemeMode,
    dynamicColor: Boolean,
    onThemeMode: (ThemeMode) -> Unit = {},
    onDynamicColor: (Boolean) -> Unit = {},
    onSave: (String, Double?, Double?) -> Unit,
    onTestRest: () -> Unit = {},
    onTestReminder: () -> Unit = {},
    onTestAchievement: () -> Unit = {},
    onEnableDaily: () -> Unit = {},
    onEnableBody: () -> Unit = {},
    onEnableWeekly: () -> Unit = {},
    onNavigatePaywall: () -> Unit,
    onNavigateCatalog: () -> Unit,
    onNavigateAchievements: () -> Unit,
    onNavigateExercise: (String) -> Unit,
    onNavigateExercises: () -> Unit = {},
) {
    var name by remember(profile?.displayName) { mutableStateOf(profile?.displayName ?: "") }
    var height by remember(profile?.heightCm) { mutableStateOf(profile?.heightCm?.toString() ?: "") }
    var weight by remember(profile?.weightKg) { mutableStateOf(profile?.weightKg?.toString() ?: "") }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("PROFILE", style = RepForgeTypeRoles.DisplayHero, color = MaterialTheme.colorScheme.onBackground)

        // Profile editor — height is the new requirement
        Column(modifier = Modifier.clip(RepForgeShapes.CardLarge).background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("YOU", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Display name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("Height cm") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Weight kg") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
            }
            if (bodyBmi != null) Text("BMI: ${String.format("%.1f", bodyBmi)} ${if (bodyBmi in 18.5..24.9) "• In Range ✓" else ""}", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.primary)
            Button(onClick = { onSave(name, height.toDoubleOrNull(), weight.toDoubleOrNull()) }, modifier = Modifier.fillMaxWidth()) { Text("Save • log weight/reps/body") }
            Text("Weight + height logged to BodyMetric (for BMI, Wilks, normalized strength). Also log per-set: weight kg • reps • RIR in Workout. History never overwritten.", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        }

        // Appearance (todo 14): persisted theme mode + Android 12+ dynamic color.
        // Controls use md.sys roles via MaterialTheme — no hardcoded colors.
        Column(modifier = Modifier.clip(RepForgeShapes.CardLarge).background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("APPEARANCE", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ThemeMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = themeMode == mode,
                        onClick = { onThemeMode(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = ThemeMode.entries.size),
                        label = { Text(mode.storageLabel.replaceFirstChar { it.uppercase() }) },
                    )
                }
            }
            SettingSwitchRow(
                title = "Wallpaper colors",
                subtitle = "Android 12+ dynamic color from your wallpaper",
                checked = dynamicColor,
                onChecked = onDynamicColor,
            )
        }

        SettingsCard(title = "Achievements", subtitle = "$achievementsUnlocked unlocked — streaks, PRs, volume, BMI", onClick = onNavigateAchievements)
        // MD3 assist chips surface real unlocked achievements (todo 14); nothing
        // is rendered when the list is empty — never fabricated badges.
        if (achievementTitles.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                achievementTitles.take(6).forEach { title ->
                    AssistChip(
                        onClick = onNavigateAchievements,
                        label = { Text(title) },
                    )
                }
            }
        }
        SettingsCard(title = "Exercises", subtitle = "60 • Home (no equipment) / Gym • Chest, Back, Legs… • 3D muscle", onClick = onNavigateExercises)
        SettingsCard(title = "Gym Programs", subtitle = "PPL • Upper/Lower • Full Body • 5×5 — 60 exercises, 3D viewer", onClick = { onNavigateExercise("bench_bb") })
        SettingsCard(title = "RepForge Pro", subtitle = "Adaptive engine • Advanced analytics • $3.99/mo", onClick = onNavigatePaywall)
        SettingsCard(title = "Health & data", subtitle = "Exactly what is connected — additive", onClick = {})
        SettingsCard(title = "Design System Catalog", subtitle = "Typography • Shapes • Motion", onClick = onNavigateCatalog)

        // Notifications — home/gym agnostic, but critical for adherence
        Column(modifier = Modifier.clip(RepForgeShapes.CardLarge).background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("NOTIFICATIONS", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            Text("Live Update is for active workout (Android 16+ promoted). Below are reminders — daily 18:00, rest-complete (heads-up), achievements, weekly summary, body log nudge.", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onEnableDaily, modifier = Modifier.weight(1f)) { Text("Daily 18:00") }
                Button(onClick = onEnableBody, modifier = Modifier.weight(1f)) { Text("Body weekly") }
                Button(onClick = onEnableWeekly, modifier = Modifier.weight(1f)) { Text("Weekly") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onTestReminder, modifier = Modifier.weight(1f)) { Text("Test reminder") }
                OutlinedButton(onClick = onTestRest, modifier = Modifier.weight(1f)) { Text("Test rest") }
                OutlinedButton(onClick = onTestAchievement, modifier = Modifier.weight(1f)) { Text("Test PR") }
            }
            Text("Needs POST_NOTIFICATIONS. Channels: reminders, rest (HIGH), achievements, body (LOW), weekly (LOW). Scheduled via WorkManager.", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }

        Column(modifier = Modifier.clip(RepForgeShapes.CardLarge).background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Log progress correctly", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("• Per set: load kg + target reps + completed reps + RIR/RPE (SetLog)\n• Per day: body weight + height → BMI, strength/bodyweight ratio\n• PR detects new estimated 1RM (Epley) and unlocks Achievement", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurface)
            Text("Height once, weight often. Re-measure height rarely; log weight after each session if you want trend.", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        }
        Text("Offline-first • On-device ML • Health data stays local", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun SettingsCard(title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RepForgeShapes.CardMedium).background(MaterialTheme.colorScheme.surfaceVariant).clickable(onClick = onClick).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(title, style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurface)
        Text(subtitle, style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
