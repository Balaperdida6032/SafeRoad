package com.absdev.saferoad.core.navigation.Profile.Edit

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.absdev.saferoad.core.navigation.model.Profile
import com.absdev.saferoad.ui.theme.GreenLogo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EditarPerfilScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid

    var name by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var pesoKg by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    LaunchedEffect(Unit) {
        userId?.let {
            db.collection("profile").document(it).get()
                .addOnSuccessListener { doc ->
                    val perfil = doc.toObject(Profile::class.java)
                    name = perfil?.name.orEmpty()
                    birthDate = perfil?.birthDate.orEmpty()
                    pesoKg = perfil?.pesoKg?.toString().orEmpty()
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
                                .height(56.dp),
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


                    Button(
                        onClick = {
                            if (!isValidDate(birthDate)) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Fecha inválida. Usa el formato dd/MM/yyyy ❗")
                                }
                            } else {
                                val data = mapOf(
                                    "name" to name,
                                    "birthDate" to birthDate,
                                    "pesoKg" to pesoKg.toFloatOrNull()
                                )
                                userId?.let {
                                    db.collection("profile").document(it).update(data)
                                        .addOnSuccessListener {
                                            navController.popBackStack()
                                        }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 42.dp)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenLogo)
                    ) {
                        Text("Guardar", color = Color.White)
                    }
                }
            }
        }
    }
}

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
