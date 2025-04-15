package com.absdev.saferoad.core.navigation.Profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.absdev.saferoad.core.navigation.navigation.Welcome
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.absdev.saferoad.core.navigation.navigation.EditarPerfil
import com.google.firebase.firestore.FirebaseFirestore
import com.absdev.saferoad.core.navigation.BottomNavigation.Admin.AdminBottomNavItem
import com.google.firebase.auth.EmailAuthProvider

@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = viewModel()
) {
    val profileState = viewModel.profile.collectAsState()
    val auth = FirebaseAuth.getInstance()

    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var showMenu by remember { mutableStateOf(false) }
    var showConfirmDelete by remember { mutableStateOf(false) }

    val navBackStackEntry = remember {
        navController.getBackStackEntry(AdminBottomNavItem.Profile.route)
    }

    DisposableEffect(navBackStackEntry) {
        val lifecycle = navBackStackEntry.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshProfile()
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

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
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Configuración")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color.DarkGray)
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar perfil", color = Color.White) },
                        onClick = {
                            showMenu = false
                            navController.navigate(EditarPerfil)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar cuenta", color = Color.Red) },
                        onClick = {
                            showMenu = false
                            showConfirmDelete = true
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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

            if (profile.role == "admin") {
                Text(text = "Rol: ${profile.role}")
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        Button(
            onClick = {
                auth.signOut()
                currentUser = null
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar sesión", color = Color.White)
        }
    }

    // AlertDialog de confirmación para eliminar cuenta
    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("¿Eliminar cuenta?", color = Color.White) },
            text = { Text("Esta acción eliminará tu perfil de forma permanente.", color = Color.White) },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDelete = false

                    val user = auth.currentUser
                    val email = user?.email.orEmpty()

                    // 🔐 Pedimos una contraseña temporal — esto deberías cambiarlo por un input real
                    val password = "123456" // ¡Pedirla al usuario con un TextField sería lo ideal!

                    val credential = EmailAuthProvider.getCredential(email, password)

                    user?.reauthenticate(credential)
                        ?.addOnSuccessListener {
                            // ✅ Reautenticación correcta, eliminar perfil
                            val userId = user.uid
                            FirebaseFirestore.getInstance().collection("profile").document(userId).delete()
                                .addOnSuccessListener {
                                    user.delete()
                                        .addOnSuccessListener {
                                            auth.signOut()
                                            currentUser = null
                                        }
                                }
                        }
                        ?.addOnFailureListener {
                            // Mostrar error si la reautenticación falla
                            println("❌ Reautenticación fallida: ${it.message}")
                        }
                }) {
                    Text("Eliminar", color = Color.Red)
                }

            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) {
                    Text("Cancelar", color = Color.White)
                }
            },
            containerColor = Color.DarkGray
        )
    }
}
