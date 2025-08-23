package com.redstonetorch.dongbaekro

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.kakao.vectormap.KakaoMapSdk
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp

// User 데이터 모델
data class User(
    val userId: String,
    val username: String,
    // 필요한 다른 사용자 정보 추가
)

@HiltAndroidApp
class DongBaekRoApp : Application() {

    // SharedPreferences 인스턴스, 앱 전체에서 로그인 데이터 관리에 사용
    private val prefs by lazy {
        getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    }
    private val gson = Gson()

    override fun onCreate() {
        super.onCreate()

        try {
            // BuildConfig에서 키를 가져오되, 빈 값일 경우 local.properties에서 직접 읽기
            var nativeKey = BuildConfig.KAKAO_NATIVE_KEY
            
            Log.d("KakaoMapInit", "BuildConfig Native Key: '$nativeKey'")
            
            // BuildConfig가 비어있다면 하드코딩된 값 사용 (임시)
            if (nativeKey.isNullOrEmpty()) {
                nativeKey = "d17493f40fc2b80fc57cb03e3abdddec" // local.properties에서 확인한 키
                Log.w("KakaoMapInit", "Using hardcoded key as BuildConfig is empty")
            }

            if (nativeKey.isEmpty()) {
                Log.e("KakaoMapInit", "KAKAO_NATIVE_KEY is still empty!")
                return
            }

            // 카카오맵 SDK 초기화
            KakaoMapSdk.init(this, nativeKey)
            Log.d("KakaoMapInit", "KakaoMapSdk initialized successfully with key: ${nativeKey.take(10)}...")

        } catch (e: Exception) {
            Log.e("KakaoMapInit", "Failed to initialize KakaoMapSdk", e)
        }
    }
    /**
     * 로그인 시 사용자 정보와 토큰을 저장합니다.
     * @param accessToken 액세스 토큰
     * @param refreshToken 리프레시 토큰
     * @param user 사용자 정보 객체
     */
    fun saveLoginData(
        accessToken: String,
        refreshToken: String,
        user: User
    ) {
        prefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .putString("user_info", gson.toJson(user))
            .apply()
    }

    /**
     * 저장된 액세스 토큰을 가져옵니다.
     * @return 액세스 토큰, 없으면 null
     */
    fun getAccessToken(): String? = prefs.getString("access_token", null)

    /**
     * 저장된 리프레시 토큰을 가져옵니다.
     * @return 리프레시 토큰, 없으면 null
     */
    fun getRefreshToken(): String? = prefs.getString("refresh_token", null)

    /**
     * 저장된 사용자 정보를 가져옵니다.
     * @return User 객체, 없으면 null
     */
    fun getUserInfo(): User? {
        val json = prefs.getString("user_info", null)
        return gson.fromJson(json, User::class.java)
    }

    /**
     * 로그아웃 시 저장된 모든 로그인 정보를 삭제합니다.
     */
    fun clearLoginData() {
        prefs.edit().clear().apply()
    }
}
