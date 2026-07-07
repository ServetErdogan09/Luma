package com.serveterdogan.lume.ui.screen.gallery

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serveterdogan.lume.domain.model.Memory
import com.serveterdogan.lume.domain.repository.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TagGalleryUiState(
    val isLoading: Boolean = true,
    val memories: List<Memory> = emptyList(),
    val galleryTitle: String = "",
    val error: String? = null
)

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class TagGalleryViewModel @Inject constructor(
    private val memoryRepository: MemoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagGalleryUiState())
    val uiState: StateFlow<TagGalleryUiState> = _uiState.asStateFlow()

    /**
     * Belirtilen anının TÜM etiketlerine göre benzer anıları getirir.
     * Galeri başlığı olarak o anının başlığını kullanır.
     */
    fun loadByMemoryId(memoryId: Int) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val memory = memoryRepository.getMemoryById(memoryId)
                if (memory == null) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }

                // Tüm etiketleri al
                val tags = memory.tag
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                // Tüm etiketlere göre benzer anıları getir (kendisi hariç)
                val similarMemories = if (tags.isNotEmpty()) {
                    memoryRepository.getAllSimilarMemories(tags, excludeId = memoryId)
                } else {
                    emptyList()
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        memories = similarMemories,
                        galleryTitle = memory.title
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
