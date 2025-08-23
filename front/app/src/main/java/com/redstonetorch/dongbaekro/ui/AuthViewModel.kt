package com.redstonetorch.dongbaekro.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redstonetorch.dongbaekro.DongBaekRoApp
import com.redstonetorch.dongbaekro.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val application: Application) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow<Boolean>(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            _isLoggedIn.value = (application as DongBaekRoApp).getAccessToken() != null
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            // TODO: Implement actual login logic here
            // For now, simulate a successful login
            val dummyUser = User(userId = "1", username = username)
            (application as DongBaekRoApp).saveLoginData("dummy_access_token", "dummy_refresh_token", dummyUser)
            _isLoggedIn.value = true
        }
    }

    fun logout() {
        viewModelScope.launch {
            (application as DongBaekRoApp).clearLoginData()
            _isLoggedIn.value = false
        }
    }
}
