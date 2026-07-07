package com.serveterdogan.lume.ui.screen.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.serveterdogan.lume.domain.repository.MemoryRepository
import com.serveterdogan.lume.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val settingsRepository: SettingsRepository,
    private val memoryRepository: MemoryRepository
) : AndroidViewModel(application) {

    private val _saveMemories = MutableStateFlow(true)
    val saveMemories: StateFlow<Boolean> = _saveMemories.asStateFlow()

    private val _aiAnalysis = MutableStateFlow(true)
    val aiAnalysis: StateFlow<Boolean> = _aiAnalysis.asStateFlow()

    private val _notifications = MutableStateFlow(true)
    val notifications: StateFlow<Boolean> = _notifications.asStateFlow()

    private val _themeMode = MutableStateFlow(false)
    val themeMode: StateFlow<Boolean> = _themeMode.asStateFlow()

    private val _storageSize = MutableStateFlow("Hesaplanıyor...")
    val storageSize: StateFlow<String> = _storageSize.asStateFlow()

    init {
        loadSettings()
        calculateStorageSize()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.saveMemoriesFlow.collect { enabled ->
                _saveMemories.value = enabled
            }
        }
        viewModelScope.launch {
            settingsRepository.aiAnalysisFlow.collect { enabled ->
                _aiAnalysis.value = enabled
            }
        }
        viewModelScope.launch {
            settingsRepository.notificationsFlow.collect { enabled ->
                _notifications.value = enabled
            }
        }
        viewModelScope.launch {
            settingsRepository.themeModeFlow.collect { isDark ->
                _themeMode.value = isDark
            }
        }
    }

    fun setSaveMemories(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSaveMemories(enabled)
        }
    }

    fun setAiAnalysis(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAiAnalysis(enabled)
        }
    }

    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotifications(enabled)
            
            // Eğer bildirimler kapatıldıysa, Alarm'ı iptal et
            val context = getApplication<Application>().applicationContext
            if (enabled) {
                com.serveterdogan.lume.util.scheduleStoryNotification(context)
            } else {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                val intent = android.content.Intent(context, com.serveterdogan.lume.util.AlarmReceiver::class.java)
                val pendingIntent = android.app.PendingIntent.getBroadcast(
                    context,
                    com.serveterdogan.lume.util.STORY_NOTIFICATION_ID,
                    intent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
            }
        }
    }

    fun setThemeMode(isDark: Boolean) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(isDark)
        }
    }

    fun deleteAllMemories() {
        viewModelScope.launch {
            memoryRepository.deleteAllMemories()
            
            // Delete images in filesDir
            val context = getApplication<Application>().applicationContext
            val filesDir = context.filesDir
            filesDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("lume_memory_") && file.name.endsWith(".jpg")) {
                    file.delete()
                }
            }
            calculateStorageSize()
        }
    }

    private fun calculateStorageSize() {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            var totalSizeBytes = 0L

            // 1. Calculate size of image files
            val filesDir = context.filesDir
            filesDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("lume_memory_") && file.name.endsWith(".jpg")) {
                    totalSizeBytes += file.length()
                }
            }

            val dbName = "luma_db"
            val dbFile = context.getDatabasePath(dbName)
            if (dbFile.exists()) totalSizeBytes += dbFile.length()
            
            val dbWalFile = context.getDatabasePath("$dbName-wal")
            if (dbWalFile.exists()) totalSizeBytes += dbWalFile.length()
            
            val dbShmFile = context.getDatabasePath("$dbName-shm")
            if (dbShmFile.exists()) totalSizeBytes += dbShmFile.length()

            // 3. Format to MB
            val sizeInMb = totalSizeBytes.toDouble() / (1024 * 1024)
            val formattedSize = String.format("%.1f MB", sizeInMb)
            
            _storageSize.value = formattedSize
        }
    }
}
