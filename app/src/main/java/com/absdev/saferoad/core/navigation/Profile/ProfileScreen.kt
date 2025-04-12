package com.absdev.saferoad.core.navigation.Profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.absdev.saferoad.core.navigation.navigation.Welcome
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = viewModel()
) {
    val profileState = viewModel.profile.collectAsState()
    val auth = FirebaseAuth.getInstance()

    // 👁 Estado reactivo del usuario actual
    var currentUser by remember { mutableStateOf(auth.currentUser) }

    // 🔁 Navega automáticamente si el usuario fue deslogueado
    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            navController.navigate(Welcome) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Esta es la pantalla de perfil", modifier = Modifier.padding(bottom = 24.dp))

        profileState.value?.let { profile ->
            profile.name?.let {
                Text(text = "Nombre: $it")
                Spacer(modifier = Modifier.height(8.dp))
            }

            profile.age?.let {
                Text(text = "Edad: $it")
                Spacer(modifier = Modifier.height(8.dp))
            }

            profile.email?.let {
                Text(text = "Email: $it")
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        Button(
            onClick = {
                auth.signOut()
                currentUser = null // 🔁 actualiza el estado y dispara LaunchedEffect
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar sesión", color = Color.White)
        }
    }
}
