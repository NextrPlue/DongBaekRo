package com.redstonetorch.dongbaekro.di // di(dependency injection) 패키지를 만들어 관리

import com.redstonetorch.dongbaekro.ui.AuthApiService
import com.redstonetorch.dongbaekro.ui.SafetyApiService // New import
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import com.redstonetorch.dongbaekro.util.TokenManager // New import

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "https://pj8bzmvmsn.ap-northeast-1.awsapprunner.com/"

    @Singleton
    @Provides
    fun provideAuthInterceptor(tokenManager: TokenManager): AuthInterceptor { // Inject TokenManager
        return AuthInterceptor(tokenManager) // Pass TokenManager to AuthInterceptor
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY // 통신 로그를 자세히 보자
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor) // Add AuthInterceptor
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideSafetyApiService(retrofit: Retrofit): SafetyApiService { // New provide method
        return retrofit.create(SafetyApiService::class.java)
    }
}