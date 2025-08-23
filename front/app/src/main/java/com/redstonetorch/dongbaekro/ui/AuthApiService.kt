package com.redstonetorch.dongbaekro.ui

import com.redstonetorch.dongbaekro.ui.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    // 회원가입 API
    @POST("api/auth/signup")
    suspend fun signup(
        @Body signupRequest: SignupRequest
    ): Response<ApiResponse<SignupData>> // 반환 타입을 ApiResponse<SignupData>로 변경

    // 로그인 API
    @POST("api/auth/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<ApiResponse<LoginData>> // 반환 타입을 ApiResponse<LoginData>로 변경

    // 프로필 업데이트 API
    @POST("api/user/profile") // Assuming an endpoint for updating user profile
    suspend fun updateUserProfile(
        @Body request: UpdateProfileRequest
    ): Response<ApiResponse<User>>
}