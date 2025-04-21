package com.absdev.saferoad.core.navigation.LoginScreen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.absdev.saferoad.ui.theme.Black
import com.absdev.saferoad.ui.theme.GreenLogo
import com.absdev.saferoad.ui.theme.ShapeButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.guru.fontawesomecomposelib.FaIcon
import com.guru.fontawesomecomposelib.FaIcons
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    auth: FirebaseAuth, navigateToHome:() -> Unit,
    navigateToAdminHome:() -> Unit,
    navController: NavController
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(text = "LOGIN SCREEN", fontSize = 25.sp, color = White)

            Spacer(modifier = Modifier.weight(1f))

            //Campo de Email
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(12.dp))

            //Campo de Password
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                maxLines = 1,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        if (passwordVisible) {
                            FaIcon(faIcon = FaIcons.Eye)
                        } else {
                            FaIcon(faIcon = FaIcons.EyeSlash)
                        }
                    }
                },
                modifier = Modifier
                    .padding(horizontal = 42.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Por favor, completa todos los campos ❗")
                        }
                    } else {
                        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = auth.currentUser?.uid
                                val db = Firebase.firestore

                                userId?.let { uid ->
                                    db.collection("profile").document(uid).get()
                                        .addOnSuccessListener { document ->
                                            if (document.exists()) {
                                                val role = document.getString("role")
                                                Log.i("ROL", "El rol es: $role")
                                                when (role) {
                                                    "user" -> navigateToHome()
                                                    "admin" -> navigateToAdminHome()
                                                    else -> scope.launch {
                                                        snackbarHostState.showSnackbar("Rol no reconocido ❌")
                                                    }
                                                }
                                            } else {
                                                Log.w("ROL", "Documento no encontrado para UID: $uid")
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Perfil no encontrado ❌")
                                                }
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.e("ROL", "Error al obtener el documento: $exception")
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Error de conexión ❌")
                                            }
                                        }
                                }
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Credenciales incorrectas ❌")
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 54.dp)
                    .border(2.dp, ShapeButton, CircleShape),
                colors = ButtonDefaults.buttonColors(containerColor = GreenLogo)
            ) {
                Text(text = "Continue", color = White)
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
