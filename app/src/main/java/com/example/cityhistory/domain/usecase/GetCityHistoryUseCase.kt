package com.example.cityhistory.domain.usecase

import com.example.cityhistory.domain.repository.CityHistoryRepository
import javax.inject.Inject

class GetCityHistoryUseCase @Inject constructor(
    private val repository: CityHistoryRepository,
) {

    suspend operator fun invoke(apiKey: String, city: String): Result<String> {
        if (apiKey.isBlank()) {
            return Result.failure(IllegalArgumentException("API key must not be empty."))
        }
        if (city.isBlank()) {
            return Result.failure(IllegalArgumentException("City name must not be empty."))
        }
        return repository.getCityHistory(apiKey = apiKey.trim(), city = city.trim())
    }
}
