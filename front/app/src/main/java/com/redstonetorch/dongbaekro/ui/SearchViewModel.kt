package com.redstonetorch.dongbaekro.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redstonetorch.dongbaekro.BuildConfig
import com.redstonetorch.dongbaekro.ui.dto.KakaoPlace
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val kakaoSearchApiService: KakaoSearchApiService
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<KakaoPlace>>(emptyList())
    val searchResults: StateFlow<List<KakaoPlace>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedPlace = MutableStateFlow<KakaoPlace?>(null)
    val selectedPlace: StateFlow<KakaoPlace?> = _selectedPlace.asStateFlow()

    fun searchPlaces(
        query: String,
        currentLat: Double? = null,
        currentLng: Double? = null
    ) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val authorization = "KakaoAK ${BuildConfig.KAKAO_REST_KEY}"
                android.util.Log.d("SearchViewModel", "API Key: ${BuildConfig.KAKAO_REST_KEY}")
                android.util.Log.d("SearchViewModel", "Authorization: $authorization")
                val response = kakaoSearchApiService.searchPlace(
                    authorization = authorization,
                    query = query,
                    longitude = currentLng,
                    latitude = currentLat,
                    radius = 20000,
                    page = 1,
                    size = 15,
                    sort = if (currentLat != null && currentLng != null) "distance" else "accuracy"
                )

                if (response.isSuccessful) {
                    val searchResponse = response.body()
                    _searchResults.value = searchResponse?.documents ?: emptyList()
                } else {
                    _error.value = "검색 중 오류가 발생했습니다: ${response.code()}"
                    _searchResults.value = emptyList()
                }
            } catch (e: Exception) {
                _error.value = "네트워크 오류가 발생했습니다: ${e.message}"
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectPlace(place: KakaoPlace) {
        _selectedPlace.value = place
    }

    fun clearSelectedPlace() {
        _selectedPlace.value = null
    }

    fun clearResults() {
        _searchResults.value = emptyList()
        _error.value = null
    }
}