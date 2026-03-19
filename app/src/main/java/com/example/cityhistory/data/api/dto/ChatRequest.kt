package com.example.cityhistory.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<MessageDto>,
    @SerialName("max_tokens") val maxTokens: Int = 512,
    val stop: List<String>? = null,
)

@Serializable
data class MessageDto(
    val role: String,
    val content: String,
)
