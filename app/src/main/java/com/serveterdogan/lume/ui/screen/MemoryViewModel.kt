package com.serveterdogan.lume.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serveterdogan.lume.domain.model.Memory
import com.serveterdogan.lume.domain.repository.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed interface MemoryUiEvent {
    data class ShowSnackbar(val message: String) : MemoryUiEvent
    object MemorySaved : MemoryUiEvent
    object MemoryUpdated : MemoryUiEvent
    object MemoryDeleted : MemoryUiEvent
}

data class MemoryUiState(
    val isLoading: Boolean = false,
    val memories: List<Memory> = emptyList(),
    val dailyMemories: List<Memory> = emptyList(),
    val error: String? = null,
    val selectedMemory: Memory? = null
)

@HiltViewModel
class MemoryViewModel @Inject constructor(
    private val memoryRepository: MemoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MemoryUiState>(MemoryUiState())
    val uiState : StateFlow<MemoryUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<MemoryUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        getAllMemories()
    }

    private fun getAllMemories() {
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
}