package com.serveterdogan.lume.domain.usecase

import android.content.Context
import android.net.Uri
import android.util.Log
import android.util.Base64
import java.io.ByteArrayOutputStream
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.serveterdogan.lume.data.remote.ChatRequest
import com.serveterdogan.lume.data.remote.ContentPart
import com.serveterdogan.lume.data.remote.ImageUrl
import com.serveterdogan.lume.data.remote.Message
import com.serveterdogan.lume.data.remote.OpenAIApiService
import com.serveterdogan.lume.domain.model.AiResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import javax.inject.Inject

class AnalyzeImageUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val openAIApiService: OpenAIApiService
) {
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend operator fun invoke(uri: Uri): Result<AiResult> = runCatching {
        Log.d("LumeAI", "AI Analiz Süreci Başladı. Uri: $uri")
        // metin taramasını yap
        val inputImage = InputImage.fromFilePath(context, uri)
        val textResult = textRecognizer.process(inputImage).await()
        val extractedText = textResult.text
        Log.d("LumeAI", "OCR'dan okunan metin: \n$extractedText")

        // Kelime sayısını bul
        val wordCount = extractedText.split("\\s+".toRegex()).count { it.isNotBlank() }
        Log.d("LumeAI", "ML Kit Tarama Bitti. Bulunan Kelime Sayısı: $wordCount")

        // Görseli Bitmape çevir
        val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
            android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.setOnPartialImageListener { _ -> true }
            }.copy(android.graphics.Bitmap.Config.ARGB_8888, true)
        } else {
            @Suppress("DEPRECATION")
            android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }

        // AI için komut
        val prompt = """
            Sen bir günlük/anı uygulamasının yapay zeka asistanısın.
            Sana verilen girdiyi analiz et ve aşağıdaki JSON formatında bir yanıt üret:
            {
              "title": "Kısa ve ilgi çekici bir başlık (en fazla 5 kelime)",
              "description": "Görsel veya metin hakkında en fazla iki cümlelik kısa ve etkileyici bir açıklama.",
              "tags": ["Etiket1", "Etiket2"] // En fazla 2 veya 3 etiket olsun. Kesinlikle 3'ü geçmesin.
            }
            
            Önemli kurallar:
            - Eğer girdi (görsel veya metin) bir ders notu, sınav kağıdı, soru veya ödev ise ve hangi derse (örn: Matematik, Fizik, Tarih, Türkçe vb.) ait olduğunu anlayabiliyorsan, o dersin adını mutlaka "tags" (etiketler) kısmına ekle.
            
            LÜTFEN yanıtını SADECE geçerli bir JSON nesnesi olarak döndür. Başına veya sonuna "```json" gibi markdown etiketleri EKLEME. Sadece ham süslü parantezlerle başlayan JSON dönsün.
        """.trimIndent()

        val jsonResponseText: String = if (wordCount > 15) {
            Log.d("LumeAI", "Senaryo A: 15 kelimeden fazla metin var. Sadece metni LLMTR'ye gönderiyoruz.")
            val request = ChatRequest(
                model = "qwen/qwen3.7-plus", 
                messages = listOf(
                    Message(
                        role = "user",
                        content = listOf(
                            ContentPart(type = "text", text = prompt),
                            ContentPart(type = "text", text = "İşte resimden çıkarılan metin:\n$extractedText")
                        )
                    )
                )
            )
            val response = openAIApiService.generateChatCompletion(request)
            response.choices.firstOrNull()?.message?.content ?: throw Exception("API boş yanıt döndürdü.")
        } else {
            Log.d("LumeAI", "Senaryo B: Metin az. Görseli Base64 yapıp LLMTR'ye (Vision) gönderiyoruz.")
            if (bitmap == null) throw Exception("Görsel bitmap'i sağlanmadı.")
            
            // Bitmap to Base64
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()
            val base64Image = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            
            val request = ChatRequest(
                model = "qwen/qwen3.7-plus", 
                messages = listOf(
                    Message(
                        role = "user",
                        content = listOf(
                            ContentPart(type = "text", text = prompt),
                            ContentPart(type = "image_url", image_url = ImageUrl(url = "data:image/jpeg;base64,$base64Image"))
                        )
                    )
                )
            )
            val response = openAIApiService.generateChatCompletion(request)
            response.choices.firstOrNull()?.message?.content ?: throw Exception("API boş yanıt döndürdü.")
        }
        
        Log.d("LumeAI", "LLMTR'den yanıt alındı: $jsonResponseText")

        // JSON formatını temizle (Garanti olması için)
        val cleanedJson = jsonResponseText.replace("```json", "").replace("```", "").trim()
        Log.d("LumeAI", "Temizlenmiş JSON: $cleanedJson")
        
        // Parse JSON
        val jsonObject = JSONObject(cleanedJson)
        val title = jsonObject.getString("title")
        val description = jsonObject.getString("description")
        val tagsArray = jsonObject.getJSONArray("tags")
        
        val tags = mutableListOf<String>()
        for (i in 0 until tagsArray.length()) {
            tags.add(tagsArray.getString(i))
        }

        Log.d("LumeAI", "Parse Başarılı -> Başlık: $title, Açıklama: $description, Etiketler: $tags")
        AiResult(title, description, tags)
    }.onFailure { e ->
        Log.e("LumeAI", "AnalyzeImageUseCase içinde hata oluştu!", e)
    }
}
