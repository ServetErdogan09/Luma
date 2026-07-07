package com.serveterdogan.lume.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.serveterdogan.lume.BuildConfig
import com.serveterdogan.lume.domain.model.DailySummary
import com.serveterdogan.lume.domain.model.Memory
import com.serveterdogan.lume.domain.repository.DailySummaryRepository
import com.serveterdogan.lume.domain.repository.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

sealed interface DailySummaryUiEvent {
    data class ShowSnackbar(val message: String) : DailySummaryUiEvent
    object SummarySaved : DailySummaryUiEvent
}

data class DailySummaryUiState(
    val isLoading: Boolean = false,
    val allSummaries: List<DailySummary> = emptyList(),
    val todaySummary: DailySummary? = null,
    val dailyMemories: List<Memory> = emptyList(),
    val error: String? = null,
    // Saat kilidi
    val isUnlocked: Boolean = false,    // 21:00 geçti mi?
    val hoursUntilUnlock: Long = 0,     // Kaç saat kaldı
    val minutesUntilUnlock: Long = 0,   // Kaç dakika kaldı
    val secondsUntilUnlock: Long = 0    // Kaç saniye kaldı
)

/** Bugün için kilit saati: 21:00 */
const val STORY_UNLOCK_HOUR = 21
const val STORY_UNLOCK_MINUTE = 0

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class DailySummaryViewModel @Inject constructor(
    private val repository: DailySummaryRepository,
    private val memoryRepository: MemoryRepository,
    private val openAIApiService: com.serveterdogan.lume.data.remote.OpenAIApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailySummaryUiState())
    val uiState: StateFlow<DailySummaryUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<DailySummaryUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        getAllSummaries()
        startCountdownTicker()
    }

    /** Her saniye saat kilidini yeniden hesaplar */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startCountdownTicker() {
        viewModelScope.launch {
            while (true) {
                updateTimeLock()
                delay(1000L) // her saniye
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateTimeLock() {
        val now = LocalTime.now()
        val unlockTime = LocalTime.of(STORY_UNLOCK_HOUR, STORY_UNLOCK_MINUTE)
        val isUnlocked = !now.isBefore(unlockTime)
        
        val totalSecondsLeft = if (isUnlocked) 0L else {
            val nowSeconds = now.hour * 3600L + now.minute * 60L + now.second
            val unlockSeconds = STORY_UNLOCK_HOUR * 3600L + STORY_UNLOCK_MINUTE * 60L
            unlockSeconds - nowSeconds
        }
        
        _uiState.update {
            it.copy(
                isUnlocked = isUnlocked,
                hoursUntilUnlock = totalSecondsLeft / 3600,
                minutesUntilUnlock = (totalSecondsLeft % 3600) / 60,
                secondsUntilUnlock = totalSecondsLeft % 60
            )
        }
    }

    private fun getAllSummaries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.getAllSummaries().collect { summaries ->
                    _uiState.update { it.copy(isLoading = false, allSummaries = summaries) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                _uiEvent.send(DailySummaryUiEvent.ShowSnackbar(e.message ?: "Özetler yüklenirken hata oluştu"))
            }
        }
    }

    fun getSummaryByDate(date: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val summaries = repository.getSummaryByDate(date)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        todaySummary = summaries.firstOrNull()
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                _uiEvent.send(DailySummaryUiEvent.ShowSnackbar(e.message ?: "Günün özeti yüklenirken hata oluştu"))
            }
        }
    }

    fun fetchInitialData(date: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // 1. O günün anılarını getir
                val memories = memoryRepository.getMemoriesByDate(date)
                _uiState.update { it.copy(dailyMemories = memories) }
                
                // 2. O güne ait özet var mı kontrol et
                val existingSummaries = repository.getSummaryByDate(date)
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        todaySummary = existingSummaries.firstOrNull()
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = e.message) 
                }
                _uiEvent.send(DailySummaryUiEvent.ShowSnackbar(e.message ?: "Veriler yüklenirken hata oluştu"))
            }
        }
    }

    fun generateSummaryForDate(date: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val memories = _uiState.value.dailyMemories
                
                // Anı yoksa
                if (memories.isEmpty()) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            todaySummary = DailySummary(id = 0, date = date, aiStory = "Bugün için kaydedilmiş bir anı bulunamadı.", isCreated = false)
                        ) 
                    }
                    return@launch
                }
                
                // Anılar varsa ama açıklamaları boşsa
                val descriptions = memories.mapNotNull { it.aiDescription }.joinToString("\n- ")
                if (descriptions.isBlank()) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            todaySummary = DailySummary(id = 0, date = date, aiStory = "Bugün kaydedilen anıların detaylı açıklaması bulunamadığı için özet oluşturulamadı.", isCreated = false)
                        ) 
                    }
                    return@launch
                }
                
                // Gemini API çağrısı
                val prompt = """
                    Sen sıcakkanlı ve motive edici bir yapay zeka asistanısın. 
                    Aşağıda kullanıcının bugüne ait anılarının açıklamaları yer alıyor. 
                    Lütfen bu açıklamaları analiz ederek, günü nasıl geçirdiğine dair kısa, akıcı ve samimi bir "Günün Hikayesi" (özeti) yaz. 
                    Özetin 3-4 cümleyi geçmesin ve hikayeleştirici bir dili olsun. Yanıtında "İşte özetin" gibi girişler yapma, doğrudan hikayeyi ver.
                    
                    Günün Anıları:
                    - $descriptions
                """.trimIndent()
                
                val request = com.serveterdogan.lume.data.remote.ChatRequest(
                    model = "deepseek-chat", // Veya istenilen bir qwen modeli
                    messages = listOf(
                        com.serveterdogan.lume.data.remote.Message(
                            role = "user",
                            content = listOf(
                                com.serveterdogan.lume.data.remote.ContentPart(
                                    type = "text",
                                    text = prompt
                                )
                            )
                        )
                    )
                )

                val response = openAIApiService.generateChatCompletion(request)
                val generatedText = response.choices.firstOrNull()?.message?.content?.trim() ?: "Özet oluşturulurken bir hata oluştu."
                
                val newSummary = DailySummary(id = 0, date = date, aiStory = generatedText, isCreated = true)
                
                // Yeni özeti kaydet
                insertSummary(newSummary)
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        todaySummary = newSummary
                    ) 
                }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = e.message) 
                }
                _uiEvent.send(DailySummaryUiEvent.ShowSnackbar(e.message ?: "Özet oluşturulurken hata oluştu"))
            }
        }
    }

    fun insertSummary(dailySummary: DailySummary) {
        viewModelScope.launch {
            try {
                repository.insertDailySummary(dailySummary)
                _uiEvent.send(DailySummaryUiEvent.SummarySaved)
            } catch (e: Exception) {
                _uiEvent.send(DailySummaryUiEvent.ShowSnackbar(e.message ?: "Özet kaydedilirken hata oluştu"))
            }
        }
    }
}