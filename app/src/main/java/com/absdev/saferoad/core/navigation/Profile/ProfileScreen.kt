package com.absdev.saferoad.core.navigation.Profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.absdev.saferoad.core.navigation.navigation.EditarPerfil
import com.google.firebase.firestore.FirebaseFirestore
import com.absdev.saferoad.core.navigation.BottomNavigation.Admin.AdminBottomNavItem
import com.absdev.saferoad.ui.theme.GreenLogo
import com.absdev.saferoad.ui.theme.ShapeButton
import com.google.firebase.auth.EmailAuthProvider
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = viewModel(),
    onLogout: () -> Unit
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

            profile.birthDate?.let { birthDate ->
                val edadCalculada = calcularEdad(birthDate)
                if (edadCalculada != null) {
                    Text(text = "Edad: $edadCalculada años")
                } else {
                    Text(text = "Edad: No disponible")
                }
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
                onLogout()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 32.dp)
                .border(2.dp, ShapeButton, CircleShape),
            colors = ButtonDefaults.buttonColors(containerColor = GreenLogo)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar sesión", color = Color.White)
        }
    }

    if (showConfirmDelete) {
        var passwordInput by remember { mutableStateOf("") }
        var isDeleting by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = {
                showConfirmDelete = false
                passwordInput = ""
                errorMessage = null
            },
            title = { Text("¿Eliminar cuenta?", color = Color.White) },
            text = {
                Column {
                    Text("Esta acción eliminará tu perfil de forma permanente.", color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            errorMessage = null
                        },
                        label = { Text("Contraseña") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation()
                    )
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(errorMessage.orEmpty(), color = Color.Red)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (passwordInput.isBlank()) {
                            errorMessage = "La contraseña no puede estar vacía"
                            return@TextButton
                        }

                        isDeleting = true
                        val user = auth.currentUser
                        val email = user?.email.orEmpty()
                        val credential = EmailAuthProvider.getCredential(email, passwordInput)

                        user?.reauthenticate(credential)
                            ?.addOnSuccessListener {
                                val userId = user.uid
                                FirebaseFirestore.getInstance().collection("profile").document(userId).delete()
                                    .addOnSuccessListener {
                                        user.delete()
                                            .addOnSuccessListener {
                                                onLogout()
                                            }
                                    }
                            }
                            ?.addOnFailureListener {
                                errorMessage = "❌ Contraseña incorrecta"
                                isDeleting = false
                            }
                    },
                    enabled = !isDeleting
                ) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showConfirmDelete = false
                    passwordInput = ""
                    errorMessage = null
                }) {
                    Text("Cancelar", color = Color.White)
                }
            },
            containerColor = Color.DarkGray
        )
    }
}

// Función para calcular la edad
fun calcularEdad(birthDate: String): Int? {
    return try {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fecha = sdf.parse(birthDate)
        val nacimiento = java.util.Calendar.getInstance().apply { time = fecha!! }
        val hoy = java.util.Calendar.getInstance()

        var edad = hoy.get(java.util.Calendar.YEAR) - nacimiento.get(java.util.Calendar.YEAR)

        if (hoy.get(java.util.Calendar.DAY_OF_YEAR) < nacimiento.get(java.util.Calendar.DAY_OF_YEAR)) {
            edad--
        }

        edad
    } catch (e: Exception) {
        null
    }
}

