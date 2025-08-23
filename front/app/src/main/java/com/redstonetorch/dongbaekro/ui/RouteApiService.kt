package com.redstonetorch.dongbaekro.ui

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RouteApiService {
    @GET("api/safety-facilities/safe-route")
    suspend fun getSafeRoute(
        @Query("originLatitude") originLatitude: Double,
        @Query("originLongitude") originLongitude: Double,
        @Query("destinationLatitude") destinationLatitude: Double,
        @Query("destinationLongitude") destinationLongitude: Double,
        @Query("preferredFacilityTypes") preferredFacilityTypes: List<String>
    ): Response<SafeRouteResponse>
}