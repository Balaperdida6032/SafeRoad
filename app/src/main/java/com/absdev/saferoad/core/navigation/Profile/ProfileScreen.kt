package com.absdev.saferoad.core.navigation.Profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.absdev.saferoad.core.navigation.navigation.Login
import com.google.firebase.auth.FirebaseAuth
import java.nio.file.WatchEvent

@Composable
fun ProfileScreen(navController: NavHostController, viewModel: ProfileViewModel = ProfileViewModel()){
    val profileState = viewModel.profile.collectAsState()

    Text("Esta es la pantalla de perfil", modifier = Modifier.padding (24.dp))
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        profileState.value?.let { profile ->
            // ✅ Solo muestra si hay información
            profile.name?.let {
                Text(text = "Nombre: $it")
            }

            Spacer(modifier = Modifier.height(8.dp))

            profile.age?.let {
                Text(text = "Edad: $it")
            }

            Spacer(modifier = Modifier.height(8.dp))

            profile.email?.let {
                Text(text = "Email: $it")

            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate(Login) {
                    popUpTo(0)
                }
            }) {
                Text("Cerrar sesión")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate(Login) {
                    popUpTo(0) // Limpia el back stack
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar sesión", color = Color.White)
        }
    }
}