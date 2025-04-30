package com.absdev.saferoad.core.navigation.SingUp

import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.absdev.saferoad.core.navigation.model.Profile
import com.absdev.saferoad.ui.theme.GreenLogo
import com.absdev.saferoad.ui.theme.ShapeButton
import com.guru.fontawesomecomposelib.FaIcon
import com.guru.fontawesomecomposelib.FaIcons
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SingScreen(auth: FirebaseAuth, navController: NavController) {
    var name by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var pesoKg by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
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
            Text(text = "Register SCREEN", fontSize = 25.sp, color = White)
            Spacer(modifier = Modifier.weight(1f))

            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                singleLine = true,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 42.dp)
                    .height(56.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                calendar.set(year, month, dayOfMonth)
                                val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                birthDate = format.format(calendar.time)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
            ) {
                TextField(
                    value = birthDate,
                    onValueChange = {},
                    label = { Text("Fecha de nacimiento") },
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 42.dp)
                        .height(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = pesoKg,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("""^\d*\.?\d*$""")))
                        pesoKg = it
                },
                label = { Text("Peso (kg)") },
                singleLine = true,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 42.dp)
                    .height(56.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 42.dp)
                    .height(56.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                    .fillMaxWidth()
                    .padding(horizontal = 42.dp)
                    .height(56.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    if (name.isBlank() || birthDate.isBlank() || email.isBlank() || password.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Por favor, completa todos los campos ❗")
                        }
                    } else {
                        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = task.result?.user?.uid
                                val profile = Profile(
                                    pesoKg = pesoKg.toFloatOrNull(),
                                    uid = userId,
                                    name = name,
                                    birthDate = birthDate,
                                    email = email,
                                    password = password,
                                    role = "user"
                                )

                                if (userId != null) {
                                    FirebaseFirestore.getInstance()
                                        .collection("profile")
                                        .document(userId)
                                        .set(profile)
                                        .addOnSuccessListener {
                                            Log.d("Firestore", "Perfil guardado con éxito")
                                        }
                                        .addOnFailureListener {
                                            Log.e("Firestore", "Error al guardar perfil", it)
                                        }
                                }

                                scope.launch {
                                    snackbarHostState.showSnackbar("Usuario registrado correctamente ✅")
                                }
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Error al registrar el usuario ❌")
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
                Text(text = "Confirmar", color = White)
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}