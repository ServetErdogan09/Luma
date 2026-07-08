package com.serveterdogan.lume.ui.screen

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.serveterdogan.lume.BuildConfig
import com.serveterdogan.lume.domain.model.AiResult
import com.serveterdogan.lume.domain.model.Memory
import com.serveterdogan.lume.domain.repository.MemoryRepository
import com.serveterdogan.lume.domain.repository.SettingsRepository
import com.serveterdogan.lume.domain.usecase.AnalyzeImageUseCase
import com.serveterdogan.lume.domain.usecase.SaveMemoryUseCase
import com.serveterdogan.lume.domain.usecase.UpdateMemoryUseCase
import com.serveterdogan.lume.util.ImageCompressor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

sealed interface MemoryUiEvent {
    data class ShowSnackbar(val message: String) : MemoryUiEvent
    object MemorySaved : MemoryUiEvent
    object MemoryUpdated : MemoryUiEvent
    object MemoryDeleted : MemoryUiEvent
}

data class MemoryUiState(
    val isLoading: Boolean = false,
    val isAiAnalyzing: Boolean = false,
    val memories: List<Memory> = emptyList(),
    val dailyMemories: List<Memory> = emptyList(),
    val similarMemories: List<Memory> = emptyList(),
    val searchResults: List<Memory> = emptyList(),
    val error: String? = null,
    val selectedMemory: Memory? = null,
    val aiResult: AiResult? = null,
    val isAiEnabled: Boolean = true
)

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class MemoryViewModel @Inject constructor(
    private val memoryRepository: MemoryRepository,
    private val analyzeImageUseCase: AnalyzeImageUseCase,
    private val settingsRepository: SettingsRepository,
    private val saveMemoryUseCase: SaveMemoryUseCase,
    private val updateMemoryUseCase: UpdateMemoryUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<MemoryUiState>(MemoryUiState())
    val uiState: StateFlow<MemoryUiState> = _uiState.asStateFlow()



    private val _uiEvent = Channel<MemoryUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadAllMemories()
        viewModelScope.launch {
            settingsRepository.aiAnalysisFlow.collect { enabled ->
                _uiState.update { it.copy(isAiEnabled = enabled) }
            }
        }
    }

    private fun loadAllMemories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                memoryRepository.getAllMemories().collect { memories ->
                    _uiState.update { it.copy(memories = memories, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
                _uiEvent.send(
                    MemoryUiEvent.ShowSnackbar(
                        e.message ?: "Anılar yüklenirken bir hata oluştu"
                    )
                )
            }
        }
    }


    fun deleteMemory(memoryId: Int) {
        viewModelScope.launch {
            try {
                memoryRepository.deleteMemoryById(memoryId)
                _uiEvent.send(MemoryUiEvent.MemoryDeleted)
            } catch (e: Exception) {
                _uiEvent.send(
                    MemoryUiEvent.ShowSnackbar(
                        e.message ?: "Anı silinirken bir hata oluştu"
                    )
                )
            }
        }
    }



    fun getMemoryById(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val selectMemory = memoryRepository.getMemoryById(id)
                _uiState.update { it.copy(isLoading = false, selectedMemory = selectMemory) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                _uiEvent.send(
                    MemoryUiEvent.ShowSnackbar(
                        e.message ?: "Anı yüklenirken bir hata oluştu"
                    )
                )
            }
        }
    }

    // benzer anıları getir
    fun getSimilarMemories(tags: List<String>, excludeId: Int) {
        viewModelScope.launch {
            try {
                if (tags.isEmpty()) {
                    _uiState.update { it.copy(similarMemories = emptyList()) }
                    return@launch
                }
                val similar = memoryRepository.getSimilarMemories(tags, excludeId)
                _uiState.update { it.copy(similarMemories = similar) }
            } catch (e: Exception) {
                // Sessizce hatayı yoksay veya logla
            }
        }
    }


    fun analyzeImage(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiAnalyzing = true, aiResult = null) }
            try {
                val result = analyzeImageUseCase(uri)
                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(
                            isAiAnalyzing = false,
                            aiResult = result.getOrNull()
                        )
                    }
                    _uiEvent.send(MemoryUiEvent.ShowSnackbar("Yapay zeka analizi başarıyla tamamlandı!"))
                } else {
                    val exception = result.exceptionOrNull()
                    Log.e("LumeAI", "ViewModel'da AI başarısız oldu", exception)
                    _uiState.update { it.copy(isAiAnalyzing = false, error = exception?.message) }
                    _uiEvent.send(
                        MemoryUiEvent.ShowSnackbar(
                            exception?.message ?: "Yapay zeka analizi başarısız oldu"
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("LumeAI", "ViewModel catch bloğuna düştü", e)
                _uiState.update { it.copy(isAiAnalyzing = false, error = e.message) }
                _uiEvent.send(
                    MemoryUiEvent.ShowSnackbar(
                        e.message ?: "Analiz sırasında bir hata oluştu"
                    )
                )
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun saveMemoryFromUI(uri: Uri, title: String, description: String, tags: List<String>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = saveMemoryUseCase.invoke(uri, title, description, tags)
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false) }
                _uiEvent.send(MemoryUiEvent.MemorySaved)
                _uiEvent.send(MemoryUiEvent.ShowSnackbar("Anı başarıyla kaydedildi!"))
            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false, error = exception.message) }
                _uiEvent.send(MemoryUiEvent.ShowSnackbar("Kaydetme hatası: ${exception.message}"))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateMemoryFromUI(memoryId: Int, uri: Uri?, existingImagePath: String?, title: String, description: String, tags: List<String>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = updateMemoryUseCase(memoryId, uri, existingImagePath, title, description, tags)
            
            result.onSuccess { updatedMemory ->
                _uiState.update { it.copy(isLoading = false, selectedMemory = updatedMemory) }
                _uiEvent.send(MemoryUiEvent.MemoryUpdated)
                _uiEvent.send(MemoryUiEvent.ShowSnackbar("Anı başarıyla güncellendi!"))
            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false, error = exception.message) }
                _uiEvent.send(MemoryUiEvent.ShowSnackbar("Güncelleme hatası: ${exception.message}"))
            }
        }
    }
}

