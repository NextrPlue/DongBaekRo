package com.redstonetorch.dongbaekro.di

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton
import com.redstonetorch.dongbaekro.util.TokenManager // New import

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager // Inject TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val accessToken = tokenManager.getAccessToken() // Get the token from TokenManager

        val requestBuilder = originalRequest.newBuilder()

        accessToken?.let {
            requestBuilder.header("Authorization", "Bearer $it")
        }

        return chain.proceed(requestBuilder.build())
    }
}