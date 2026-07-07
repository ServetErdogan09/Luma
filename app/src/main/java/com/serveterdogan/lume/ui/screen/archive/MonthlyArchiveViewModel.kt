package com.serveterdogan.lume.ui.screen.archive

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serveterdogan.lume.domain.model.Memory
import com.serveterdogan.lume.domain.repository.MemoryRepository
import com.serveterdogan.lume.util.toFormattedString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MonthlyArchiveUiState(
    val isLoading: Boolean = false,
    val memories: List<Memory> = emptyList(),
    val error: String? = null
)

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class MonthlyArchiveViewModel @Inject constructor(
    private val memoryRepository: MemoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(MonthlyArchiveUiState())
    val uiState: StateFlow<MonthlyArchiveUiState> = _uiState.asStateFlow()

    private val monthYear: String = checkNotNull(savedStateHandle["monthYear"])

    init {
        loadMemoriesForMonth()
    }

    private fun loadMemoriesForMonth() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                memoryRepository.getAllMemories().collect { allMemories ->
                    // Sadece seçilen aya ait olanları filtrele
                    val filteredMemories = allMemories.filter { memory ->
                        memory.date.toFormattedString("MMMM yyyy") == monthYear
                    }

                    // Tarihe göre Yeniden Eskiye (Descending) sırala
                    val sortedMemories = filteredMemories.sortedByDescending { it.date }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            memories = sortedMemories
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
