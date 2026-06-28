package com.serveterdogan.lume.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serveterdogan.lume.domain.model.DailySummary
import com.serveterdogan.lume.domain.repository.DailySummaryRepository
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

sealed interface DailySummaryUiEvent {
    data class ShowSnackbar(val message: String) : DailySummaryUiEvent
    object SummarySaved : DailySummaryUiEvent
}

data class DailySummaryUiState(
    val isLoading: Boolean = false,
    val allSummaries: List<DailySummary> = emptyList(),
    val todaySummary: DailySummary? = null,
    val error: String? = null
)

@HiltViewModel
class DailySummaryViewModel @Inject constructor(
    private val repository: DailySummaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailySummaryUiState())
    val uiState: StateFlow<DailySummaryUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<DailySummaryUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        getAllSummaries()
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
                val summaries = repository.getSummariesByDateRange(date)
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