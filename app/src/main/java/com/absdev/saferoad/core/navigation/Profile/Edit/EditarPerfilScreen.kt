package com.absdev.saferoad.core.navigation.Profile.Edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.absdev.saferoad.core.navigation.model.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun EditarPerfilScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid

    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        userId?.let {
            db.collection("profile").document(it).get()
                .addOnSuccessListener { doc ->
                    val perfil = doc.toObject(Profile::class.java)
                    name = perfil?.name.orEmpty()
                    age = perfil?.age?.toString().orEmpty()
                    loading = false
                }
        }
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Editar perfil", fontSize = 24.sp, color = Color.White)
            Spacer(modifier = Modifier.height(24.dp))

            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Edad") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val data = mapOf(
                        "name" to name,
                        "age" to age.toIntOrNull()
                    )

                    userId?.let {
                        db.collection("profile").document(it).update(data)
                            .addOnSuccessListener {
                                navController.popBackStack()
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar")
            }
        }
    }
}
