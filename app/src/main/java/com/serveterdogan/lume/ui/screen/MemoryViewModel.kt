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
import com.serveterdogan.lume.domain.usecase.AnalyzeImageUseCase
import com.serveterdogan.lume.util.ImageCompressor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    private val settingsRepository: com.serveterdogan.lume.domain.repository.SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<MemoryUiState>(MemoryUiState())
    val uiState : StateFlow<MemoryUiState> = _uiState.asStateFlow()

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
                _uiEvent.send(MemoryUiEvent.ShowSnackbar(e.message ?: "Anılar yüklenirken bir hata oluştu"))
            }
        }
    }

    fun insertMemory(memory: Memory) {
        viewModelScope.launch {
            try {
                memoryRepository.insertMemory(memory)
                _uiEvent.send(MemoryUiEvent.MemorySaved)
            } catch (e: Exception) {
                _uiEvent.send(MemoryUiEvent.ShowSnackbar(e.message ?: "Anı kaydedilirken bir hata oluştu"))
            }
        }
    }

    fun deleteMemory(memoryId : Int) {
        viewModelScope.launch {
            try {
                memoryRepository.deleteMemoryById(memoryId)
                _uiEvent.send(MemoryUiEvent.MemoryDeleted)
            } catch (e: Exception) {
                _uiEvent.send(MemoryUiEvent.ShowSnackbar(e.message ?: "Anı silinirken bir hata oluştu"))
            }
        }
    }

    fun updateMemory(memory: Memory) {
        viewModelScope.launch {
            try {
                memoryRepository.updateMemory(memory)
                _uiEvent.send(MemoryUiEvent.MemoryUpdated)
            } catch (e: Exception) {
                _uiEvent.send(MemoryUiEvent.ShowSnackbar(e.message ?: "Anı güncellenirken bir hata oluştu"))
            }
        }
    }

    fun getMemoryById(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val selectMemory = memoryRepository.getMemoryById(id)
                _uiState.update { it.copy(isLoading = false , selectedMemory = selectMemory) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                _uiEvent.send(MemoryUiEvent.ShowSnackbar(e.message ?: "Anı yüklenirken bir hata oluştu"))
            }
        }
    }

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

    fun searchMemories(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                if (query.isBlank()) {
                    _uiState.update { it.copy(searchResults = emptyList(), isLoading = false) }
                } else {
                    val results = memoryRepository.searchMemories(query)
                    _uiState.update { it.copy(searchResults = results, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun getMemoriesByDateRange(date : LocalDate){
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val memories =  memoryRepository.getMemoriesByDate(date)
                _uiState.update { it.copy(isLoading = false , dailyMemories = memories) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
                _uiEvent.send(MemoryUiEvent.ShowSnackbar(e.message ?: "Tarihe göre filtreleme başarısız oldu"))
            }
        }
    }

    fun analyzeImage(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiAnalyzing = true, aiResult = null) }
            try {
                val result = analyzeImageUseCase(uri)
                if (result.isSuccess) {
                    _uiState.update { it.copy(isAiAnalyzing = false, aiResult = result.getOrNull()) }
                    _uiEvent.send(MemoryUiEvent.ShowSnackbar("Yapay zeka analizi başarıyla tamamlandı!"))
                } else {
                    val exception = result.exceptionOrNull()
                    Log.e("LumeAI", "ViewModel'da AI başarısız oldu", exception)
                    _uiState.update { it.copy(isAiAnalyzing = false, error = exception?.message) }
                    _uiEvent.send(MemoryUiEvent.ShowSnackbar(exception?.message ?: "Yapay zeka analizi başarısız oldu"))
                }
            } catch (e: Exception) {
                Log.e("LumeAI", "ViewModel catch bloğuna düştü", e)
                _uiState.update { it.copy(isAiAnalyzing = false, error = e.message) }
                _uiEvent.send(MemoryUiEvent.ShowSnackbar(e.message ?: "Analiz sırasında bir hata oluştu"))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveMemoryFromUI(uri: Uri, title: String, description: String, tags: List<String>) {
        viewModelScope.launch {
            Log.d("LumeSave", "Anı kaydetme süreci başladı. Title: $title")
            _uiState.update { it.copy(isLoading = true) }
            try {

                Log.d("LumeSave", "Görsel sıkıştırılıyor ve Internal Storage'a kaydediliyor...")
                val localImagePath = ImageCompressor.compressAndSaveImage(context, uri)
                Log.d("LumeSave", "Görsel başarıyla kaydedildi. Yol: $localImagePath")
                
                // Ayarlarda galeriye kaydetme açıksa MediaStore'a da kopyala
                val shouldSaveToGallery = settingsRepository.saveMemoriesFlow.first()
                if (shouldSaveToGallery) {
                    ImageCompressor.copyToGallery(context, localImagePath)
                    Log.d("LumeSave", "Görsel Galeriye (MediaStore) kopyalandı.")
                }
                
                val combinedTags = tags.joinToString(", ")
                
                val currentDate = LocalDate.now()
                val currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                
                // 4. Memory Objesini oluştur
                val newMemory = Memory(
                    id = 0, // Room auto-generates
                    imagePath = localImagePath,
                    date = currentDate,
                    time = currentTime,
                    tag = combinedTags,
                    title = title,
                    aiDescription = description
                )
                
                Log.d("LumeSave", "Oluşturulan Memory Objesi Room'a yazılıyor: $newMemory")
                // 5. Veritabanına kaydet
                memoryRepository.insertMemory(newMemory)
                Log.d("LumeSave", "Room veritabanına başarıyla eklendi!")
                
                _uiState.update { it.copy(isLoading = false) }
                _uiEvent.send(MemoryUiEvent.MemorySaved)
                _uiEvent.send(MemoryUiEvent.ShowSnackbar("Anı başarıyla kaydedildi!"))
                
            } catch (e: Exception) {
                Log.e("LumeSave", "Kaydetme sırasında hata oluştu!", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                _uiEvent.send(MemoryUiEvent.ShowSnackbar("Kaydetme hatası: ${e.message}"))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateMemoryFromUI(memoryId: Int, uri: Uri?, existingImagePath: String?, title: String, description: String, tags: List<String>) {
        viewModelScope.launch {
            Log.d("LumeUpdate", "Anı güncelleme süreci başladı. ID: $memoryId")
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Determine image path
                val finalImagePath = if (uri != null) {
                    Log.d("LumeUpdate", "Yeni görsel sıkıştırılıyor...")
                    val localImagePath = ImageCompressor.compressAndSaveImage(context, uri)
                    
                    val shouldSaveToGallery = settingsRepository.saveMemoriesFlow.first()
                    if (shouldSaveToGallery) {
                        ImageCompressor.copyToGallery(context, localImagePath)
                    }
                    localImagePath
                } else {
                    existingImagePath ?: ""
                }
                
                val combinedTags = tags.joinToString(", ")
                
                // Get existing memory to preserve date/time if needed, or update to now?
                // Usually we preserve the original date and time. We can fetch it first:
                val existingMemory = memoryRepository.getMemoryById(memoryId) 
                    ?: throw Exception("Güncellenmek istenen anı bulunamadı.")
                
                val updatedMemory = existingMemory.copy(
                    imagePath = finalImagePath,
                    tag = combinedTags,
                    title = title,
                    aiDescription = description
                )
                
                Log.d("LumeUpdate", "Güncellenen Memory Objesi Room'a yazılıyor: $updatedMemory")
                memoryRepository.updateMemory(updatedMemory)
                
                _uiState.update { it.copy(isLoading = false, selectedMemory = updatedMemory) }
                _uiEvent.send(MemoryUiEvent.MemoryUpdated)
                _uiEvent.send(MemoryUiEvent.ShowSnackbar("Anı başarıyla güncellendi!"))
                
            } catch (e: Exception) {
                Log.e("LumeUpdate", "Güncelleme sırasında hata oluştu!", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                _uiEvent.send(MemoryUiEvent.ShowSnackbar("Güncelleme hatası: ${e.message}"))
            }
        }
    }
}