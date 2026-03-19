package com.example.cityhistory.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cityhistory.domain.usecase.GetCityHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@HiltViewModel
class CityHistoryViewModel @Inject constructor(
    private val getCityHistoryUseCase: GetCityHistoryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CityHistoryUiState>(CityHistoryUiState.Idle)
    val uiState: StateFlow<CityHistoryUiState> = _uiState.asStateFlow()

    fun fetchHistory(
        apiKey: String,
        city: String,
        maxTokens: Int,
        stopSequences: List<String>,
    ) {
        if (_uiState.value is CityHistoryUiState.Loading) return

        val trimmedKey = apiKey.trim()
        val trimmedCity = city.trim()

        if (trimmedKey.isBlank()) {
            _uiState.value = CityHistoryUiState.Error("Please enter your Groq API key.")
            return
        }
        if (trimmedCity.isBlank()) {
            _uiState.value = CityHistoryUiState.Error("Please enter a city name.")
            return
        }

        _uiState.value = CityHistoryUiState.Loading

        viewModelScope.launch {
            try {
                val result = withTimeout(30_000L) {
                    getCityHistoryUseCase(
                        apiKey = trimmedKey,
                        city = trimmedCity,
                        maxTokens = maxTokens,
                        stopSequences = stopSequences,
                    )
                }
                _uiState.value = result.fold(
                    onSuccess = { CityHistoryUiState.Success(it) },
                    onFailure = { CityHistoryUiState.Error(it.message ?: "An unexpected error occurred.") },
                )
            } catch (e: TimeoutCancellationException) {
                _uiState.value = CityHistoryUiState.Error("Request timed out. Please try again.")
            }
        }
    }

    fun resetState() {
        _uiState.value = CityHistoryUiState.Idle
    }
}
