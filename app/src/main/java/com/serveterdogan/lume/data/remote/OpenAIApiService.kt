package com.serveterdogan.lume.data.remote

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface OpenAIApiService {

    @POST("v1/chat/completions")
    suspend fun generateChatCompletion(
        @Body request: ChatRequest
    ): ChatResponse
}
