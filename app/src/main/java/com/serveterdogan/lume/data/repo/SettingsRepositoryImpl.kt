package com.serveterdogan.lume.data.repo

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.serveterdogan.lume.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private object PreferencesKeys {
        val SAVE_MEMORIES = booleanPreferencesKey("save_memories")
        val AI_ANALYSIS = booleanPreferencesKey("ai_analysis")
        val NOTIFICATIONS = booleanPreferencesKey("notifications")
        val THEME_MODE = booleanPreferencesKey("theme_mode")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    override val saveMemoriesFlow: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.SAVE_MEMORIES] ?: true // Default is true
        }

    override val aiAnalysisFlow: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.AI_ANALYSIS] ?: true // Default is true
        }

    override val notificationsFlow: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS] ?: true // Default is true
        }

    override val themeModeFlow: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.THEME_MODE] ?: true // Default is true
        }

    override val onboardingCompletedFlow: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false // Default is false
        }

    override suspend fun setSaveMemories(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SAVE_MEMORIES] = enabled
        }
    }

    override suspend fun setAiAnalysis(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AI_ANALYSIS] = enabled
        }
    }

    override suspend fun setNotifications(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS] = enabled
        }
    }

    override suspend fun setThemeMode(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = isDark
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }
}
