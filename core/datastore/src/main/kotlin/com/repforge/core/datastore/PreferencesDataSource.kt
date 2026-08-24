package com.repforge.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.repforge.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    object Keys {
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val USER_ID = stringPreferencesKey("user_id")
        val SELECTED_ROUTINE = stringPreferencesKey("selected_routine")
        val MODEL_VERSION = stringPreferencesKey("model_version")
        val HEALTH_WEIGHT_ENABLED = booleanPreferencesKey("health_weight")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    }

    val onboardingDone: Flow<Boolean> = dataStore.data.map { it[Keys.ONBOARDING_DONE] ?: false }
    val userId: Flow<String?> = dataStore.data.map { it[Keys.USER_ID] }
    val themeMode: Flow<ThemeMode> = dataStore.data.map { ThemeMode.fromStorageLabel(it[Keys.THEME_MODE]) }
    val dynamicColor: Flow<Boolean> = dataStore.data.map { it[Keys.DYNAMIC_COLOR] ?: false }

    suspend fun setOnboardingDone(done: Boolean) { dataStore.edit { it[Keys.ONBOARDING_DONE] = done } }
    suspend fun setUserId(id: String) { dataStore.edit { it[Keys.USER_ID] = id } }
    suspend fun setThemeMode(mode: ThemeMode) { dataStore.edit { it[Keys.THEME_MODE] = mode.storageLabel } }
    suspend fun setDynamicColor(enabled: Boolean) { dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled } }
}
