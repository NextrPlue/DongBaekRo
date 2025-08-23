package com.redstonetorch.dongbaekro.ui

import com.redstonetorch.dongbaekro.ui.dto.KakaoSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KakaoSearchApiService {
    
    @GET("v2/local/search/keyword.json")
    suspend fun searchPlace(
        @Header("Authorization") authorization: String,
        @Query("query") query: String,
        @Query("x") longitude: Double? = null,
        @Query("y") latitude: Double? = null,
        @Query("radius") radius: Int = 20000,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 15,
        @Query("sort") sort: String = "accuracy"
    ): Response<KakaoSearchResponse>
}