package com.absdev.saferoad.core.navigation.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ParticipanteCarreraStartScreen(
    idCarrera: String,
    navController: NavController
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationRequest = remember {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
    }
    val locationCallback = rememberUpdatedState(newValue = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
            val db = FirebaseDatabase.getInstance().reference
            val location: Location = locationResult.lastLocation ?: return
            Log.d("Participante", "Ubicación obtenida: lat=${location.latitude}, lng=${location.longitude}")
            val userLocation = mapOf(
                "lat" to location.latitude,
                "lng" to location.longitude,
                "timestamp" to System.currentTimeMillis()
            )
            db.child("carreras").child(idCarrera).child("corredores").child(userId)
                .setValue(userLocation)
                .addOnSuccessListener {
                    Log.d("Participante", "Ubicación enviada a Firebase con éxito")
                }
                .addOnFailureListener {
                    Log.e("Participante", "Error al enviar ubicación: ${it.message}")
                }
        }
    })

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    var carreraIniciada by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!carreraIniciada) {
            Button(onClick = {
                if (locationPermissionState.status.isGranted) {
                    carreraIniciada = true
                } else {
                    locationPermissionState.launchPermissionRequest()
                }
            }) {
                Text("Comenzar carrera")
            }

            if (!locationPermissionState.status.isGranted) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Debes aceptar el permiso de ubicación para comenzar", color = Color.Red)
            }

        } else {
            Text("¡Tu ubicación se está enviando!")

            LaunchedEffect(Unit) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback.value,
                        Looper.getMainLooper()
                    )
                }
            }
        }
    }
}
