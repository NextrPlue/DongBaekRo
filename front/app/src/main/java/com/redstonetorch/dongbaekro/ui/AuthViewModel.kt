package com.redstonetorch.dongbaekro.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redstonetorch.dongbaekro.ui.LoginRequest
import com.redstonetorch.dongbaekro.ui.SignupRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.redstonetorch.dongbaekro.util.TokenManager // New import

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authApi: AuthApiService,
    private val tokenManager: TokenManager // Inject TokenManager
) : ViewModel() {

    // 1. 로그인 상태를 저장할 StateFlow를 추가합니다.
    //    - _isLoggedIn: ViewModel 내부에서만 값을 변경할 수 있는 변수 (private)
    //    - isLoggedIn: 외부(Activity/Composable)에서는 읽기만 가능한 변수 (public)
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val request = LoginRequest(email, password)
                val response = authApi.login(request)

                if (response.isSuccessful && response.body()?.status == "success") {
                    val accessToken = response.body()?.data?.accessToken
                    if (accessToken != null) {
                        tokenManager.saveAccessToken(accessToken) // Save token
                        // 2. 로그인 성공 시, 상태 값을 true로 변경합니다.
                        _isLoggedIn.value = true
                        // Populate current user (simplified for now)
                        _currentUser.value = User(id = email, name = "User", email = email, phone = "") // Placeholder
                    }
                } else {
                    // 로그인 실패 처리
                }
            } catch (e: Exception) {
                // 네트워크 에러 처리
            }
        }
    }

    fun updateUserProfile(name: String, email: String, phone: String) {
        viewModelScope.launch {
            try {
                val request = UpdateProfileRequest(name, email, phone)
                val response = authApi.updateUserProfile(request)

                if (response.isSuccessful && response.body()?.status == "success") {
                    _currentUser.value = response.body()?.data // Update current user with response
                    println("프로필 업데이트 성공: ${response.body()?.message}")
                } else {
                    println("프로필 업데이트 실패: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                println("프로필 업데이트 에러: ${e.message}")
            }
        }
    }

    // 3. 로그아웃 기능 추가 (필요 시)
    fun logout() {
        tokenManager.clearAccessToken() // Clear token
        _isLoggedIn.value = false
    }

    // 회원가입 함수 (로그인과 거의 동일한 구조)
    fun signup(name: String, email: String, password: String, phone: String) {
        viewModelScope.launch {
            try {
                val request = SignupRequest(name, email, password, phone)
                val response = authApi.signup(request)

                if (response.isSuccessful) {
                    println("회원가입 성공: ${response.body()?.message}")
                    // TODO: 로그인 화면으로 이동 또는 자동 로그인 처리
                } else {
                    println("회원가입 실패: ${response.errorBody()?.string()}")
                    // TODO: 사용자에게 실패 원인(예: 중복된 이메일) 보여주기
                }
            } catch (e: Exception) {
                println("회원가입 에러: ${e.message}")
            }
        }
    }
}