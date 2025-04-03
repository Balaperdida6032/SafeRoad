package com.absdev.saferoad

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(navigateToLogin:() -> Unit = {}, navigateToSign:() -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize(),horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.weight(1f))
        Text(text = "Welcome Screen", fontSize = 25.sp)
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = { navigateToLogin() }) {
            Text(text = "Navegar a Login")
        }
        Button(onClick = { navigateToSign() }) {
            Text(text = "Register")
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}