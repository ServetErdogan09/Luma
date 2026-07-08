package com.serveterdogan.lume.domain.usecase

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.RequiresApi
import android.os.Build
import com.serveterdogan.lume.domain.model.Memory
import com.serveterdogan.lume.domain.repository.MemoryRepository
import com.serveterdogan.lume.domain.repository.SettingsRepository
import com.serveterdogan.lume.util.ImageCompressor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateMemoryUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository,
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend operator fun invoke(memoryId: Int, uri: Uri?, existingImagePath: String?, title: String, description: String, tags: List<String>): Result<Memory> {
        return try {
            val finalImagePath = if (uri != null) {
                Log.d("LumeUpdate", "UseCase: Yeni görsel sıkıştırılıyor...")
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
            
            val existingMemory = memoryRepository.getMemoryById(memoryId) 
                ?: throw Exception("Güncellenmek istenen anı bulunamadı.")
            
            val updatedMemory = existingMemory.copy(
                imagePath = finalImagePath,
                tag = combinedTags,
                title = title,
                aiDescription = description
            )
            
            memoryRepository.updateMemory(updatedMemory)
            Result.success(updatedMemory)
        } catch (e: Exception) {
            Log.e("LumeUpdate", "UseCase: Güncelleme sırasında hata oluştu!", e)
            Result.failure(e)
        }
    }
}
