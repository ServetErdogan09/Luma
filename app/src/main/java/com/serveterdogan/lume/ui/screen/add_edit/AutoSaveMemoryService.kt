package com.serveterdogan.lume.ui.screen.add_edit

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.serveterdogan.lume.BuildConfig
import com.serveterdogan.lume.MainActivity
import com.serveterdogan.lume.R
import com.serveterdogan.lume.data.remote.OpenAIApiService
import com.serveterdogan.lume.domain.model.Memory
import com.serveterdogan.lume.domain.repository.MemoryRepository
import com.serveterdogan.lume.navigation.Screen
import com.serveterdogan.lume.util.ImageCompressor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/**
 * Arka planda çalışan servis:
 * 1. Gelen görseli Internal Storage'a kopyalar
 * 2. Gemini AI ile analiz eder (başlık + açıklama + etiket)
 * 3. Memory olarak veritabanına kaydeder
 * 4. Bildirim gönderir: "Anın kaydedildi ✓ | Görüntüle | Geri Al"
 *
 * Kullanıcı hiçbir şeye dokunmak zorunda değil.
 */
@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.O)
class AutoSaveMemoryService : Service() {

    @Inject
    lateinit var memoryRepository: MemoryRepository

    @Inject
    lateinit var openAIApiService: OpenAIApiService

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeTasks = AtomicInteger(0)

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val CHANNEL_ID_PROGRESS = "lume_auto_save_progress"
        const val CHANNEL_ID_RESULT = "lume_auto_save_result"
        const val NOTIF_ID_PROGRESS = 9001
        const val NOTIF_ID_RESULT = 9002

        fun start(context: Context, imageUri: Uri) {
            val intent = Intent(context, AutoSaveMemoryService::class.java).apply {
                putExtra(EXTRA_IMAGE_URI, imageUri.toString())
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        startForeground(NOTIF_ID_PROGRESS, buildProgressNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val uriString = intent?.getStringExtra(EXTRA_IMAGE_URI)
        if (uriString == null) {
            stopSelf(startId)
            return START_NOT_STICKY
        }

        val uri = Uri.parse(uriString)
        activeTasks.incrementAndGet()
        serviceScope.launch {
            processAndSave(uri, startId)
        }

        return START_NOT_STICKY
    }

    private suspend fun processAndSave(uri: Uri, startId: Int) {
        try {
            Log.d("AutoSave", "Görsel işleniyor: $uri")

            // 1. Görseli internal storage'a kopyala
            val localPath = ImageCompressor.compressAndSaveImage(this@AutoSaveMemoryService, uri)
            Log.d("AutoSave", "Görsel kaydedildi: $localPath")

            // 2. Qwen AI ile analiz et
            val aiResult = analyzeWithQwen(uri)
            Log.d("AutoSave", "AI sonucu: $aiResult")

            // 3. Memory oluştur ve kaydet
            val now = LocalDate.now()
            val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

            val memory = Memory(
                id = 0,
                imagePath = localPath,
                date = now,
                time = time,
                tag = aiResult.tags.joinToString(", "),
                title = aiResult.title,
                aiDescription = aiResult.description
            )

            memoryRepository.insertMemory(memory)
            Log.d("AutoSave", "Memory kaydedildi: ${memory.title}")

            // Kaydedilen memory'nin ID'sini al (son eklenen)
            val saved = memoryRepository.searchMemories(aiResult.title).firstOrNull()

            // 4. Başarı bildirimi gönder
            showSuccessNotification(aiResult.title, saved?.id)

        } catch (e: Exception) {
            Log.e("AutoSave", "Hata oluştu", e)
            showErrorNotification()
        } finally {
            val remaining = activeTasks.decrementAndGet()
            if (remaining == 0) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            }
            stopSelf(startId)
        }
    }

    private suspend fun analyzeWithQwen(uri: Uri): AiAnalysisResult {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() ?: throw Exception("Görsel okunamadı")
            inputStream.close()

            val base64Image = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)

            val prompt = """
                Sen bir günlük/anı uygulamasının yapay zeka asistanısın.
                Sana verilen görseli analiz et ve aşağıdaki JSON formatında bir yanıt üret:
                {
                  "title": "Kısa ve ilgi çekici bir başlık (en fazla 5 kelime)",
                  "description": "Görsel hakkında en fazla iki cümlelik kısa ve etkileyici bir açıklama.",
                  "tags": ["Etiket1", "Etiket2"] // En fazla 2 veya 3 etiket olsun. Kesinlikle 3'ü geçmesin.
                }
                
                Önemli kurallar:
                - Eğer girdi (görsel) bir ders notu, sınav kağıdı, soru veya ödev ise ve hangi derse (örn: Matematik, Fizik, Tarih, Türkçe vb.) ait olduğunu anlayabiliyorsan, o dersin adını mutlaka "tags" (etiketler) kısmına ekle.
                
                LÜTFEN yanıtını SADECE geçerli bir JSON nesnesi olarak döndür. Başına veya sonuna "```json" gibi markdown etiketleri EKLEME. Sadece ham süslü parantezlerle başlayan JSON dönsün.
            """.trimIndent()

            val request = com.serveterdogan.lume.data.remote.ChatRequest(
                model = "qwen/qwen3.7-plus", 
                messages = listOf(
                    com.serveterdogan.lume.data.remote.Message(
                        role = "user",
                        content = listOf(
                            com.serveterdogan.lume.data.remote.ContentPart(type = "text", text = prompt),
                            com.serveterdogan.lume.data.remote.ContentPart(type = "image_url", image_url = com.serveterdogan.lume.data.remote.ImageUrl(url = "data:image/jpeg;base64,$base64Image"))
                        )
                    )
                )
            )

            val response = openAIApiService.generateChatCompletion(request)
            val jsonResponseText = response.choices.firstOrNull()?.message?.content ?: throw Exception("API boş yanıt döndürdü.")
            
            val cleanedJson = jsonResponseText.replace("```json", "").replace("```", "").trim()
            val jsonObject = org.json.JSONObject(cleanedJson)
            val title = jsonObject.getString("title")
            val description = jsonObject.getString("description")
            val tagsArray = jsonObject.getJSONArray("tags")
            
            val tags = mutableListOf<String>()
            for (i in 0 until tagsArray.length()) {
                tags.add(tagsArray.getString(i))
            }

            AiAnalysisResult(
                title = title,
                description = description,
                tags = tags
            )

        } catch (e: Exception) {
            Log.e("AutoSave", "AI analizi başarısız, varsayılan değerler kullanılıyor", e)
            val now = LocalDate.now()
            AiAnalysisResult(
                title = "${now.dayOfMonth} ${monthName(now.monthValue)} Anısı",
                description = "Otomatik kaydedilen bir anı.",
                tags = listOf("anı")
            )
        }
    }

    private fun monthName(month: Int) = when (month) {
        1 -> "Ocak"; 2 -> "Şubat"; 3 -> "Mart"; 4 -> "Nisan"
        5 -> "Mayıs"; 6 -> "Haziran"; 7 -> "Temmuz"; 8 -> "Ağustos"
        9 -> "Eylül"; 10 -> "Ekim"; 11 -> "Kasım"; else -> "Aralık"
    }

    // ── BİLDİRİMLER ──────────────────────────────────────────────────────────

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        val progressChannel = NotificationChannel(
            CHANNEL_ID_PROGRESS,
            "Anı Kaydediliyor",
            NotificationManager.IMPORTANCE_LOW
        ).apply { setShowBadge(false) }

        val resultChannel = NotificationChannel(
            CHANNEL_ID_RESULT,
            "Anı Kaydedildi",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        manager.createNotificationChannel(progressChannel)
        manager.createNotificationChannel(resultChannel)
    }

    private fun buildProgressNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID_PROGRESS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Lume")
            .setContentText("✨ Anın hazırlanıyor...")
            .setProgress(0, 0, true)
            .setOngoing(true)
            .build()
    }

    private fun showSuccessNotification(title: String, memoryId: Int?) {
        val manager = getSystemService(NotificationManager::class.java)

        // Görüntüle butonu
        val viewIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (memoryId != null) {
                putExtra("navigate_to", Screen.MemoryDetail.createRoute(memoryId))
            }
        }
        val viewPendingIntent = PendingIntent.getActivity(
            this, 0, viewIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID_RESULT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Anı kaydedildi ✓")
            .setContentText("\"$title\" Lume'a eklendi")
            .setAutoCancel(true)
            .setContentIntent(viewPendingIntent)
            .addAction(0, "Görüntüle →", viewPendingIntent)
            .build()

        manager.notify(NOTIF_ID_RESULT, notification)
    }

    private fun showErrorNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID_RESULT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Lume")
            .setContentText("Anı kaydedilirken bir hata oluştu")
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIF_ID_RESULT, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}

data class AiAnalysisResult(
    val title: String,
    val description: String,
    val tags: List<String>
)
