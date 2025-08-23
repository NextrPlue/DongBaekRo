package com.redstonetorch.dongbaekro.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun MainScreen(viewModel: AuthViewModel = hiltViewModel()) {
    AppNavigation(viewModel = viewModel)
}
