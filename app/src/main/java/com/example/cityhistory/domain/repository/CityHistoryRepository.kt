package com.example.cityhistory.domain.repository

interface CityHistoryRepository {

    suspend fun getCityHistory(
        apiKey: String,
        city: String,
        maxTokens: Int,
        stopSequences: List<String>,
    ): Result<String>
}
