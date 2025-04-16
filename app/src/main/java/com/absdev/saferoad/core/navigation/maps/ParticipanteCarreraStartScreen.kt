package com.absdev.saferoad.core.navigation.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.math.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ParticipanteCarreraStartScreen(
    idCarrera: String,
    navController: NavController
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val firestore = FirebaseFirestore.getInstance()

    val locationRequest = remember {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
    }

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var carreraIniciada by remember { mutableStateOf(false) }
    var ruta by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var carreraFinalizada by remember { mutableStateOf(false) }

    val locationCallback = rememberUpdatedState(newValue = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
            val db = FirebaseDatabase.getInstance().reference
            val location: Location = locationResult.lastLocation ?: return
            val latLng = LatLng(location.latitude, location.longitude)
            userLocation = latLng

            val userLocMap = mapOf(
                "lat" to location.latitude,
                "lng" to location.longitude,
                "timestamp" to System.currentTimeMillis()
            )
            db.child("carreras").child(idCarrera).child("corredores").child(userId)
                .setValue(userLocMap)
                .addOnSuccessListener {
                    Log.d("Participante", "Ubicación enviada con éxito")
                }
                .addOnFailureListener {
                    Log.e("Participante", "Error al enviar ubicación: ${it.message}")
                }
        }
    })

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // 🔁 Escuchar en tiempo real si se finalizó la carrera
    LaunchedEffect(idCarrera) {
        firestore.collection("carreras").document(idCarrera)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val started = snapshot?.getBoolean("isStarted") ?: true
                carreraFinalizada = !started
            }
    }

    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        } else {
            val doc = firestore.collection("carreras").document(idCarrera).get().await()
            val puntos = doc.get("ruta") as? List<Map<String, Double>>
            ruta = puntos?.mapNotNull {
                val lat = it["lat"]
                val lng = it["lng"]
                if (lat != null && lng != null) LatLng(lat, lng) else null
            } ?: emptyList()
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
            Text("¡Tu ubicación se está enviando!", color = Color.White)

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

            Spacer(modifier = Modifier.height(24.dp))

            val totalDist = remember(ruta) { calcularDistanciaTotal(ruta) }
            val progreso = calcularProgreso(userLocation, ruta)
            val restante = totalDist * (1 - progreso.coerceIn(0f, 1f))

            if (ruta.isNotEmpty() && userLocation != null) {
                Text("Distancia restante: ${"%.2f".format(restante)} km", color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = progreso.coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = Color.Green
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Progreso: ${(progreso * 100).roundToInt()}%", color = Color.White)
            }

            // Mostrar botón para salir si la carrera finalizó
            if (carreraFinalizada) {
                Spacer(modifier = Modifier.height(32.dp))
                Text("La carrera ha finalizado", color = Color.Red)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    navController.navigate("Home") {
                        popUpTo("Home") { inclusive = true }
                    }
                }) {
                    Text("Salir de la carrera")
                }
            }
        }
    }
}

fun calcularDistanciaTotal(ruta: List<LatLng>): Float {
    var total = 0f
    for (i in 0 until ruta.size - 1) {
        val start = ruta[i]
        val end = ruta[i + 1]
        val result = FloatArray(1)
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, result)
        total += result[0]
    }
    return total / 1000f // en km
}

fun calcularProgreso(actual: LatLng?, ruta: List<LatLng>): Float {
    if (actual == null || ruta.size < 2) return 0f

    var distanciaRecorrida = 0f
    var distanciaTotal = 0f

    for (i in 0 until ruta.size - 1) {
        val start = ruta[i]
        val end = ruta[i + 1]
        val tramo = FloatArray(1)
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, tramo)
        distanciaTotal += tramo[0]

        val proyeccion = proyectarPuntoSobreSegmento(actual, start, end)
        val hastaProyeccion = FloatArray(1)
        Location.distanceBetween(start.latitude, start.longitude, proyeccion.latitude, proyeccion.longitude, hastaProyeccion)

        val estaEnTramo = estaEntre(actual, start, end)
        if (estaEnTramo) {
            distanciaRecorrida += hastaProyeccion[0]
            break
        } else {
            distanciaRecorrida += tramo[0]
        }
    }
    return (distanciaRecorrida / distanciaTotal).coerceIn(0f, 1f)
}

fun proyectarPuntoSobreSegmento(p: LatLng, a: LatLng, b: LatLng): LatLng {
    val apx = p.longitude - a.longitude
    val apy = p.latitude - a.latitude
    val abx = b.longitude - a.longitude
    val aby = b.latitude - a.latitude
    val ab2 = abx * abx + aby * aby
    val ap_ab = apx * abx + apy * aby
    val t = (ap_ab / ab2).coerceIn(0.0, 1.0)
    return LatLng(a.latitude + aby * t, a.longitude + abx * t)
}

fun estaEntre(p: LatLng, a: LatLng, b: LatLng): Boolean {
    val crossproduct = (p.latitude - a.latitude) * (b.longitude - a.longitude) - (p.longitude - a.longitude) * (b.latitude - a.latitude)
    if (abs(crossproduct) > 1e-6) return false

    val dotproduct = (p.latitude - a.latitude) * (b.latitude - a.latitude) + (p.longitude - a.longitude) * (b.longitude - a.longitude)
    if (dotproduct < 0) return false

    val squaredlengthba = (b.latitude - a.latitude) * (b.latitude - a.latitude) + (b.longitude - a.longitude) * (b.longitude - a.longitude)
    if (dotproduct > squaredlengthba) return false

    return true
}
