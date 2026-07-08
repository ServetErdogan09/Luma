package com.serveterdogan.lume.domain.usecase

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.serveterdogan.lume.domain.model.Memory
import com.serveterdogan.lume.domain.repository.MemoryRepository
import com.serveterdogan.lume.domain.repository.SettingsRepository
import com.serveterdogan.lume.util.ImageCompressor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class SaveMemoryUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository,
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend operator fun invoke(uri: Uri, title: String, description: String, tags: List<String>): Result<Unit> {
        return try {
            Log.d("LumeSave", "UseCase: Görsel sıkıştırılıyor ve Internal Storage'a kaydediliyor...")
            val localImagePath = ImageCompressor.compressAndSaveImage(context, uri)
            
            val shouldSaveToGallery = settingsRepository.saveMemoriesFlow.first()
            if (shouldSaveToGallery) {
                ImageCompressor.copyToGallery(context, localImagePath)
            }
            
            val combinedTags = tags.joinToString(", ")
            val currentDate = LocalDate.now()
            val currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            
            val newMemory = Memory(
                id = 0,
                imagePath = localImagePath,
                date = currentDate,
                time = currentTime,
                tag = combinedTags,
                title = title,
                aiDescription = description
            )

           return withContext(Dispatchers.IO){
               try {
                   memoryRepository.insertMemory(newMemory)
                   Result.success(Unit)
               }catch (e: Exception){
                   Result.failure(e)
               }
           }
        } catch (e: Exception) {
            Log.e("LumeSave", "UseCase: Kaydetme sırasında hata oluştu!", e)
            Result.failure(e)
        }
    }
}
