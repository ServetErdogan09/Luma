package com.serveterdogan.lume.data.remote

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    val model: String,
    val messages: List<Message>,
    val max_tokens: Int? = null,
    val temperature: Double? = 0.7
)

data class Message(
    val role: String,
    val content: List<ContentPart>
)

data class ContentPart(
    val type: String,
    val text: String? = null,
    val image_url: ImageUrl? = null
)

data class ImageUrl(
    val url: String
)

data class ChatResponse(
    val id: String,
    val choices: List<Choice>
)

data class Choice(
    val index: Int,
    val message: ResponseMessage
)

data class ResponseMessage(
    val role: String,
    val content: String
)
