package com.absdev.saferoad.core.navigation.Home.CarreraDetail

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.absdev.saferoad.core.navigation.model.Carrera
import com.absdev.saferoad.core.navigation.navigation.EditarCarrera
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun CarreraDetailScreen(carrera: Carrera, navController: NavController) {
    var showConfigButton by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var carreraActualizada by remember { mutableStateOf<Carrera?>(null) }
    var showConfirmDelete by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()

    // Cargar datos actualizados de Firestore
    LaunchedEffect(Unit) {
        carrera.id?.let { id ->
            db.collection("carreras").document(id).get()
                .addOnSuccessListener { snapshot ->
                    carreraActualizada = snapshot.toObject(Carrera::class.java)
                }
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        uid?.let {
            db.collection("profile").document(it).get()
                .addOnSuccessListener { document ->
                    val isAdmin = document.getString("role") == "admin"
                    val isCreator = carrera.userId == uid
                    showConfigButton = isAdmin && isCreator
                }
        }
    }

    val carreraVisible = carreraActualizada ?: carrera

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }

            if (showConfigButton) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuración",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = {
                                showMenu = false
                                navController.currentBackStackEntry?.savedStateHandle?.set("carrera", carreraVisible)
                                navController.navigate(EditarCarrera)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar carrera", color = Color.Red) },
                            onClick = {
                                showMenu = false
                                showConfirmDelete = true
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val imageBitmap = carreraVisible.image?.let { base64 ->
            try {
                val imageBytes = Base64.decode(base64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }

        imageBitmap?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = carreraVisible.name.orEmpty(),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = carreraVisible.description.orEmpty(),
            color = Color.White
        )

        if (showConfirmDelete) {
            AlertDialog(
                onDismissRequest = { showConfirmDelete = false },
                title = { Text("¿Eliminar carrera?", color = Color.White) },
                text = { Text("Esta acción no se puede deshacer.", color = Color.White) },
                confirmButton = {
                    TextButton(onClick = {
                        showConfirmDelete = false
                        // Eliminar de Firestore
                        carrera.id?.let { id ->
                            FirebaseFirestore.getInstance().collection("carreras")
                                .document(id)
                                .delete()
                                .addOnSuccessListener {
                                    navController.popBackStack() // volver después de eliminar
                                }
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
}
