package com.redstonetorch.dongbaekro.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Define UI state for RouteViewModel
sealed class RouteUiState {
    object Loading : RouteUiState()
    data class Success(val data: SafeRouteData) : RouteUiState()
    data class Error(val message: String) : RouteUiState()
    object Idle : RouteUiState()
}

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val routeApiService: RouteApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow<RouteUiState>(RouteUiState.Idle)
    val uiState: StateFlow<RouteUiState> = _uiState

    fun searchSafeRoute(
        originLatitude: Double,
        originLongitude: Double,
        destinationLatitude: Double,
        destinationLongitude: Double,
        preferredFacilityTypes: List<String>
    ) {
        _uiState.value = RouteUiState.Loading
        viewModelScope.launch {
            try {
                val response = routeApiService.getSafeRoute(
                    originLatitude,
                    originLongitude,
                    destinationLatitude,
                    destinationLongitude,
                    preferredFacilityTypes
                )

                if (response.isSuccessful && response.body()?.status == "success") {
                    response.body()?.data?.let { data ->
                        _uiState.value = RouteUiState.Success(data)
                    } ?: run {
                        _uiState.value = RouteUiState.Error("Safe route data is null.")
                    }
                } else {
                    _uiState.value = RouteUiState.Error(
                        "API Error: ${response.errorBody()?.string() ?: response.message()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = RouteUiState.Error("Network Error: ${e.message ?: "Unknown error"}")
            }
        }
    }
}