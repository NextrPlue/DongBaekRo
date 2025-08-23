package com.redstonetorch.dongbaekro.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource

@Composable
fun FeatureButton(label: String, icon: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier) {
        Column {
            Icon(painter = painterResource(id = icon), contentDescription = label)
            Text(text = label)
        }
    }
}
