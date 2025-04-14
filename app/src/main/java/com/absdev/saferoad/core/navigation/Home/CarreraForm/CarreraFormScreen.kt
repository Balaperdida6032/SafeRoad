package com.absdev.saferoad.core.navigation.Home.CarreraForm

import android.util.Log
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
import androidx.navigation.NavController
import com.absdev.saferoad.core.navigation.model.Carrera
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun CarreraFormScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = { Text("URL de imagen") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                loading = true
                val nuevaCarrera = Carrera(
                    name = name,
                    description = description,
                    image = imageUrl
                )
                db.collection("carreras")
                    .add(nuevaCarrera)
                    .addOnSuccessListener {
                        Log.i("Firestore", "Carrera creada con éxito")
                        navController.popBackStack() // Volver atrás al Home
                    }
                    .addOnFailureListener {
                        Log.e("Firestore", "Error al crear carrera", it)
                    }
                    .addOnCompleteListener {
                        loading = false
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text(text = if (loading) "Guardando..." else "Crear carrera")
        }
    }
}
