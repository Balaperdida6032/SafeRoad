package com.absdev.saferoad.core.navigation.Home.CarreraDetail

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.absdev.saferoad.core.navigation.navigation.CarreraDetailScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun DefinirRutaCarreraScreen(carreraId: String, navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var puntosRuta by remember { mutableStateOf(listOf<LatLng>()) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(-30.9056, -55.5500), // Rivera
            15f
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.weight(1f),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                puntosRuta = puntosRuta + latLng
            }
        ) {
            Polyline(
                points = puntosRuta,
                color = Color.Green,
                width = 5f
            )

            puntosRuta.forEach { point ->
                Marker(
                    state = MarkerState(position = point),
                    title = "Punto"
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { puntosRuta = emptyList() }) {
                Text("Limpiar")
            }
            Button(
                onClick = {
                    guardarRutaEnFirestore(carreraId, puntosRuta) {
                        navController.popBackStack()
                        navController.navigate(CarreraDetailScreen)
                    }
                },
                enabled = puntosRuta.size > 1
            ) {
                Text("Guardar recorrido")
            }
        }
    }
}

private fun guardarRutaEnFirestore(
    carreraId: String,
    ruta: List<LatLng>,
    onSuccess: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val rutaMapeada = ruta.map { punto ->
        hashMapOf(
            "lat" to punto.latitude,
            "lng" to punto.longitude
        )
    }

    db.collection("carreras").document(carreraId)
        .update("ruta", rutaMapeada)
        .addOnSuccessListener {
            Log.i("Ruta", "Ruta guardada correctamente")
            onSuccess()
        }
        .addOnFailureListener {
            Log.e("Ruta", "Error al guardar ruta: ${it.message}")
        }
}