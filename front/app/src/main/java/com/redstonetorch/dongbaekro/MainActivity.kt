package com.redstonetorch.dongbaekro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.redstonetorch.dongbaekro.ui.AuthViewModel
import com.redstonetorch.dongbaekro.ui.LoginScreen
import com.redstonetorch.dongbaekro.ui.MainScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

            if (isLoggedIn) {
                MainScreen(viewModel = authViewModel)
            } else {
                LoginScreen(viewModel = authViewModel)
            }
        }
    }
}
