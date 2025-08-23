package com.redstonetorch.dongbaekro.ui

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import com.redstonetorch.dongbaekro.ui.SafetyFacility // Import the new DTO
import com.redstonetorch.dongbaekro.ui.ApiResponse // Import ApiResponse

interface SafetyApiService {
    @GET("api/safety-facilities/by-code")
    suspend fun getSafetyFacilities(
        @Query("code") code: String,
        @Query("type") type: String
    ): Response<ApiResponse<List<SafetyFacility>>>

    @GET("api/safety-facilities/region-code")
    suspend fun getRegionCode(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): Response<ApiResponse<String>> // Assuming data is directly the region code string
}