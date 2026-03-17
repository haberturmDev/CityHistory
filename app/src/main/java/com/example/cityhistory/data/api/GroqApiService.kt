package com.example.cityhistory.data.api

import com.example.cityhistory.data.api.dto.ChatRequest
import com.example.cityhistory.data.api.dto.ChatResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GroqApiService {

    @POST("openai/v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: ChatRequest,
    ): ChatResponse
}
