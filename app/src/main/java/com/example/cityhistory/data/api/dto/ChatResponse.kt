package com.example.cityhistory.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatResponse(
    val id: String? = null,
    val choices: List<ChoiceDto>? = null,
    val error: ErrorDto? = null,
)

@Serializable
data class ChoiceDto(
    val index: Int? = null,
    val message: ResponseMessageDto? = null,
    @SerialName("finish_reason") val finishReason: String? = null,
)

@Serializable
data class ResponseMessageDto(
    val role: String? = null,
    val content: String? = null,
)

@Serializable
data class ErrorDto(
    val message: String? = null,
    val type: String? = null,
    val code: String? = null,
)
