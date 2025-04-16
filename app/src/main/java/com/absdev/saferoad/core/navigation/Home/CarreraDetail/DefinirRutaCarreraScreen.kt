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
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.ui.platform.LocalContext
import com.absdev.saferoad.core.navigation.model.CalidadRed
import com.absdev.saferoad.core.navigation.model.TrayectoConectividad

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun DefinirRutaCarreraScreen(carreraId: String, navController: NavController) {
    val context = LocalContext.current
    var puntosRuta by remember { mutableStateOf(listOf<TrayectoConectividad>()) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(-30.9056, -55.5500),
            15f
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.weight(1f),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                val calidad = obtenerCalidadRed(context)
                puntosRuta = puntosRuta + TrayectoConectividad(latLng, calidad)
            }
        ) {
            puntosRuta.forEachIndexed { i, punto ->
                if (i > 0) {
                    val anterior = puntosRuta[i - 1]
                    val color = when (anterior.calidadRed) {
                        CalidadRed.BUENA -> Color.Green
                        CalidadRed.MEDIA -> Color.Yellow
                        CalidadRed.MALA -> Color.Red
                    }
                    Polyline(
                        points = listOf(anterior.latLng, punto.latLng),
                        color = color,
                        width = 5f
                    )
                }

                Marker(
                    state = MarkerState(position = punto.latLng),
                    title = "Punto ${i + 1}"
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
                    navController.previousBackStackEntry?.savedStateHandle?.set("rutaConCalidad", puntosRuta)
                    navController.popBackStack()
                },
                enabled = puntosRuta.size > 1
            ) {
                Text("Guardar recorrido")
            }
        }
    }
}


fun obtenerCalidadRed(context: Context): CalidadRed {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

    return when {
        capabilities == null -> CalidadRed.MALA
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> CalidadRed.BUENA
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
            if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                CalidadRed.MEDIA
            } else CalidadRed.MALA
        }
        else -> CalidadRed.MALA
    }
}

