package com.serveterdogan.lume.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.FileInputStream

object ImageCompressor {

    private const val MAX_WIDTH = 1920
    private const val MAX_HEIGHT = 1080
    private const val COMPRESS_QUALITY = 85

    suspend fun compressAndSaveImage(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        // 1. Dosyanın orijinal boyutlarını (sadece metadata olarak) oku
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, options)
        }

        // 2. Küçültme oranını (inSampleSize) hesapla
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT)
        options.inJustDecodeBounds = false

        // 3. Resmi hesaplanan orana göre küçülterek hafızaya (RAM) al
        val sampledBitmap = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, options)
        } ?: throw Exception("Görsel çözümlenemedi")

        // 4. Resmi Internal Storage (Uygulamanın kendi klasörü) içerisine JPEG olarak %85 kaliteyle kaydet
        val fileName = "lume_memory_${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, fileName)

        FileOutputStream(file).use { outputStream ->
            sampledBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, outputStream)
        }

        // 5. RAM'deki geçici resmi temizle
        sampledBitmap.recycle()

        // 6. Kalıcı dosya yolunu döndür
        file.absolutePath
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    suspend fun copyToGallery(context: Context, localFilePath: String) = withContext(Dispatchers.IO) {
        val file = File(localFilePath)
        if (!file.exists()) return@withContext

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Luma_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Luma")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val uri = context.contentResolver.insert(collection, values)
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                FileInputStream(file).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(it, values, null, null)
            }
        }
    }

    fun createTempImageUri(context: Context): Uri {
        val tempFile = File.createTempFile("camera_img", ".jpg", context.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }
}
