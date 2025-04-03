package com.absdev.saferoad

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.absdev.saferoad.core.navigation.NavigationWrapper
import com.absdev.saferoad.ui.theme.SafeRoadTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        enableEdgeToEdge()
        setContent {

            SafeRoadTheme {
                NavigationWrapper(auth)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser!=null){
            // navega al home
            Log.i("ElBala", "Estoy logeado")

        }else {
            Log.i("ElBala", "No Estoy logeado")
        }
    }
}
