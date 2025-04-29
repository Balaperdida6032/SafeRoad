package com.absdev.saferoad.core.navigation.Profile.Edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.absdev.saferoad.core.navigation.model.Profile
import com.absdev.saferoad.ui.theme.GreenLogo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun EditarPerfilScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid

    var name by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        userId?.let {
            db.collection("profile").document(it).get()
                .addOnSuccessListener { doc ->
                    val perfil = doc.toObject(Profile::class.java)
                    name = perfil?.name.orEmpty()
                    birthDate = perfil?.birthDate.orEmpty()
                    loading = false
                }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(padding)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color.White
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Editar perfil", fontSize = 24.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(24.dp))

                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre") },
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TextField(
                        value = birthDate,
                        onValueChange = { birthDate = it },
                        label = { Text("Fecha de nacimiento (dd/MM/yyyy)") },
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (!isValidDate(birthDate)) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Fecha inválida. Usa el formato dd/MM/yyyy ❗")
                                }
                            } else {
                                val data = mapOf(
                                    "name" to name,
                                    "birthDate" to birthDate
                                )
                                userId?.let {
                                    db.collection("profile").document(it).update(data)
                                        .addOnSuccessListener {
                                            navController.popBackStack()
                                        }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenLogo)
                    ) {
                        Text("Guardar", color = Color.White)
                    }
                }
            }
        }
    }
}

// Reutilizamos el mismo validador:
fun isValidDate(dateStr: String): Boolean {
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.isLenient = false
        sdf.parse(dateStr)
        true
    } catch (e: Exception) {
        false
    }
}
