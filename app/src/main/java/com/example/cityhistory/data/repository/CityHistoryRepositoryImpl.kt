package com.example.cityhistory.data.repository

import com.example.cityhistory.data.api.GroqApiService
import com.example.cityhistory.data.api.dto.ChatRequest
import com.example.cityhistory.data.api.dto.MessageDto
import com.example.cityhistory.domain.repository.CityHistoryRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val MODEL = "llama3-70b-8192"
private const val MAX_RETRIES = 2

@Singleton
class CityHistoryRepositoryImpl @Inject constructor(
    private val apiService: GroqApiService,
) : CityHistoryRepository {

    private val cache = HashMap<String, String>()

    override suspend fun getCityHistory(apiKey: String, city: String): Result<String> {
        val cacheKey = city.lowercase().trim()
        cache[cacheKey]?.let { return Result.success(it) }

        return fetchWithRetry(apiKey, city, cacheKey)
    }

    private suspend fun fetchWithRetry(
        apiKey: String,
        city: String,
        cacheKey: String,
        attempt: Int = 0,
    ): Result<String> {
        return try {
            val request = ChatRequest(
                model = MODEL,
                messages = listOf(
                    MessageDto(
                        role = "user",
                        content = "Provide a concise historical overview of $city",
                    )
                ),
                maxTokens = 512,
            )
            val response = apiService.getChatCompletion(
                authorization = "Bearer $apiKey",
                request = request,
            )

            val apiError = response.error?.message
            if (apiError != null) {
                return Result.failure(Exception(apiError))
            }

            val content = response.choices
                ?.firstOrNull()
                ?.message
                ?.content
                ?.trim()

            if (content.isNullOrEmpty()) {
                Result.failure(Exception("Received an empty response from the API."))
            } else {
                cache[cacheKey] = content
                Result.success(content)
            }
        } catch (e: IOException) {
            if (attempt < MAX_RETRIES) {
                fetchWithRetry(apiKey, city, cacheKey, attempt + 1)
            } else {
                Result.failure(Exception("Network error. Please check your connection and try again."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "An unexpected error occurred."))
        }
    }
}
