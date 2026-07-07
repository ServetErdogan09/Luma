package com.serveterdogan.lume.ui.screen.search

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serveterdogan.lume.data.remote.ChatRequest
import com.serveterdogan.lume.data.remote.ContentPart
import com.serveterdogan.lume.data.remote.Message
import com.serveterdogan.lume.data.remote.OpenAIApiService
import com.serveterdogan.lume.domain.model.Memory
import com.serveterdogan.lume.domain.repository.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val searchResults: List<Memory> = emptyList(),
    val isSearching: Boolean = false,
    val isAiLoading: Boolean = false,
    val aiAnswer: String? = null,
    val hasSearched: Boolean = false,
    val error: String? = null
)

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val memoryRepository: MemoryRepository,
    private val openAIApiService: OpenAIApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var aiJob: Job? = null

    /**
     * Kullanıcı her harf yazarken çağrılır.
     * Sadece normal veritabanı araması yapar (400ms debounce).
     * AI çağrısı YAPILMAZ — kullanıcı Enter'a basana kadar bekler.
     */
    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query, aiAnswer = null) }

        searchJob?.cancel()

        if (query.isBlank()) {
            _uiState.update {
                it.copy(
                    searchResults = emptyList(),
                    isSearching = false,
                    hasSearched = false
                )
            }
            return
        }

        // Sadece DB araması — 400ms debounce
        searchJob = viewModelScope.launch {
            delay(400)
            performSearch(query)
        }
    }

    /**
     * Kullanıcı klavyede "Ara" (Enter) tuşuna bastığında çağrılır.
     * Bu noktada AI yanıtı üretilir.
     */
    fun onSearchSubmit() {
        val query = _uiState.value.query
        if (query.isBlank()) return

        // Devam eden AI işini iptal et (önceki sorgu için olabilir)
        aiJob?.cancel()
        aiJob = viewModelScope.launch {
            generateAiAnswer(query)
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            try {
                val results = memoryRepository.searchMemories(query)
                _uiState.update {
                    it.copy(
                        searchResults = results,
                        isSearching = false,
                        hasSearched = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSearching = false, error = e.message, hasSearched = true)
                }
            }
        }
    }

    private fun generateAiAnswer(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true, aiAnswer = null) }
            try {
                val matchedMemories = memoryRepository.searchMemories(query)

                if (matchedMemories.isEmpty()) {
                    _uiState.update { it.copy(isAiLoading = false) }
                    return@launch
                }

                val memorySummary = matchedMemories.take(10).joinToString("\n") { memory ->
                    "- Başlık: ${memory.title}, Tarih: ${memory.date}, Etiket: ${memory.tag}, Açıklama: ${memory.aiDescription}"
                }

                val prompt = """
                    Sen Lume adlı bir anı uygulamasının yapay zeka asistanısın. 
                    Kullanıcı şunu sordu: "$query"
                    
                    Kullanıcının bu sorguyla ilgili anıları:
                    $memorySummary
                    
                    Lütfen bu anılara dayanarak kullanıcının sorusunu kısa ve samimi bir şekilde Türkçe yanıtla. 
                    Yanıtın 2-3 cümleyi geçmesin. Doğrudan yanıtı ver, "İşte yanıtın:" gibi girişler kullanma.
                """.trimIndent()

                val request = ChatRequest(
                    model = "deepseek-chat", // Veya qwen
                    messages = listOf(
                        Message(
                            role = "user",
                            content = listOf(ContentPart(type = "text", text = prompt))
                        )
                    )
                )
                
                val response = openAIApiService.generateChatCompletion(request)
                val generatedText = response.choices.firstOrNull()?.message?.content?.trim()

                _uiState.update { it.copy(isAiLoading = false, aiAnswer = generatedText) }

            } catch (e: Exception) {
                _uiState.update { it.copy(isAiLoading = false, error = e.message) }
            }
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        aiJob?.cancel()
        _uiState.update { SearchUiState() }
    }
}
