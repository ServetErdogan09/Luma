package com.serveterdogan.lume.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val saveMemoriesFlow: Flow<Boolean>
    val aiAnalysisFlow: Flow<Boolean>
    val notificationsFlow: Flow<Boolean>
    val themeModeFlow: Flow<Boolean>
    val onboardingCompletedFlow: Flow<Boolean>
    
    suspend fun setSaveMemories(enabled: Boolean)
    suspend fun setAiAnalysis(enabled: Boolean)
    suspend fun setNotifications(enabled: Boolean)
    suspend fun setThemeMode(isDark: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
}
