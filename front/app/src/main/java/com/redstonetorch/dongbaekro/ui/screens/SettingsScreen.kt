package com.redstonetorch.dongbaekro.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.redstonetorch.dongbaekro.ui.AuthViewModel

@Composable
fun SettingsScreen(viewModel: AuthViewModel = hiltViewModel()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column {
            Text(text = "Settings Screen")
            Button(onClick = { viewModel.logout() }) {
                Text("Logout")
            }
        }
    }
}
