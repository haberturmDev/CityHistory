package com.example.cityhistory.presentation

sealed class CityHistoryUiState {
    data object Idle : CityHistoryUiState()
    data object Loading : CityHistoryUiState()
    data class Success(val history: String) : CityHistoryUiState()
    data class Error(val message: String) : CityHistoryUiState()
}
