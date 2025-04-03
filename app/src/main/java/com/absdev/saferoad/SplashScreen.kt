package com.absdev.saferoad

import android.util.Log
import android.window.SplashScreen
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navigateToWelcome: () -> Unit){
    var auth: FirebaseAuth
    LaunchedEffect(Unit) {
        delay(2000) // 2 segundos
        navigateToWelcome()
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.weight(1f))
        Text(text = "SAFE ROAD", fontSize = 25.sp)
        Spacer(modifier = Modifier.weight(1f))
    }
}