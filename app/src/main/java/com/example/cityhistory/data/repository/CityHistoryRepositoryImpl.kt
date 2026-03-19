package com.example.cityhistory.data.repository

import com.example.cityhistory.data.api.GroqApiService
import com.example.cityhistory.data.api.dto.ChatRequest
import com.example.cityhistory.data.api.dto.MessageDto
import com.example.cityhistory.domain.repository.CityHistoryRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException

private const val MODEL = "llama-3.3-70b-versatile"
private const val MAX_RETRIES = 2

@Singleton
class CityHistoryRepositoryImpl @Inject constructor(
    private val apiService: GroqApiService,
) : CityHistoryRepository {

    private val cache = HashMap<String, String>()

    override suspend fun getCityHistory(
        apiKey: String,
        city: String,
        maxTokens: Int,
        stopSequences: List<String>,
    ): Result<String> {
        val cacheKey = "${city.lowercase().trim()}|$maxTokens|${stopSequences.joinToString(",")}"
        cache[cacheKey]?.let { return Result.success(it) }

        return fetchWithRetry(apiKey, city, cacheKey, maxTokens = maxTokens, stopSequences = stopSequences)
    }

    private suspend fun fetchWithRetry(
        apiKey: String,
        city: String,
        cacheKey: String,
        maxTokens: Int,
        stopSequences: List<String>,
        attempt: Int = 0,
    ): Result<String> {
        return try {
            val request = ChatRequest(
                model = MODEL,
                messages = listOf(
                    MessageDto(
                        role = "system",
                        content = """You are a knowledgeable city historian. Always respond using exactly this format, with no extra text before or after:
##Brief History: [2-3 sentences on the city's historical origins and key events]
##Modern state of city: [1-2 sentences on the city today]
##Random fact: [one surprising or little-known fact about the city]""",
                    ),
                    MessageDto(
                        role = "user",
                        content = "Tell me about $city.",
                    ),
                ),
                maxTokens = maxTokens,
                stop = stopSequences.ifEmpty { null },
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
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            if (attempt < MAX_RETRIES) {
                fetchWithRetry(apiKey, city, cacheKey, maxTokens, stopSequences, attempt + 1)
            } else {
                Result.failure(Exception("Network error. Please check your connection and try again."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "An unexpected error occurred."))
        }
    }
}
