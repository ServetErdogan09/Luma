package com.serveterdogan.lume.ui.screen.archive

import android.os.Build
import androidx.annotation.RequiresApi
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

data class ArchiveUiState(
    val isLoading: Boolean = false,
    val highlightedMemory: Memory? = null,
    val groupedMemories: Map<String, List<Memory>> = emptyMap(),
    val error: String? = null
)

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val memoryRepository: MemoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState: StateFlow<ArchiveUiState> = _uiState.asStateFlow()

    init {
        loadArchive()
    }

    private fun loadArchive() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                memoryRepository.getAllMemories().collect { allMemories ->
                    if (allMemories.isEmpty()) {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                highlightedMemory = null,
                                groupedMemories = emptyMap()
                            ) 
                        }
                        return@collect
                    }

                   // en son kullananı getir
                    val highlighted = allMemories.first()

                    val grouped = allMemories.groupBy { memory ->
                        memory.date.toFormattedString("MMMM yyyy")
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            highlightedMemory = highlighted,
                            groupedMemories = grouped
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
