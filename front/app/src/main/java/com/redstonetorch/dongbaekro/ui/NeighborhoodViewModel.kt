package com.redstonetorch.dongbaekro.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NeighborhoodViewModel @Inject constructor(
    private val safetyApiService: SafetyApiService
) : ViewModel() {

    private val _safetyFacilities = MutableStateFlow<List<SafetyFacility>>(emptyList())
    val safetyFacilities: StateFlow<List<SafetyFacility>> = _safetyFacilities

    private val _regionCode = MutableStateFlow<String?>(null)
    val regionCode: StateFlow<String?> = _regionCode

    fun getRegionCode(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                val response = safetyApiService.getRegionCode(latitude, longitude)
                if (response.isSuccessful && response.body()?.status == "success") {
                    _regionCode.value = response.body()?.data
                    println("Fetched region code: ${_regionCode.value}")
                } else {
                    println("API Error fetching region code: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                println("Network Error fetching region code: ${e.message}")
            }
        }
    }

    fun getSafetyFacilities(type: String) {
        viewModelScope.launch {
            _regionCode.value?.let { code ->
                try {
                    val response = safetyApiService.getSafetyFacilities(code, type)
                    if (response.isSuccessful && response.body()?.status == "success") {
                        _safetyFacilities.value = response.body()?.data ?: emptyList()
                        println("Fetched ${_safetyFacilities.value.size} safety facilities of type $type for code $code") // Log the size
                    } else {
                        // Handle API error
                        println("API Error fetching safety facilities: ${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    // Handle network error
                    println("Network Error fetching safety facilities: ${e.message}")
                }
            } ?: run {
                println("Region code is not available. Cannot fetch safety facilities.")
            }
        }
    }
}