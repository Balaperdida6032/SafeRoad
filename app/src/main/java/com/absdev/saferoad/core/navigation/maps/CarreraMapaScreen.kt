package com.absdev.saferoad.core.navigation.maps

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import com.absdev.saferoad.core.navigation.model.CorredorInfo
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.google.firebase.database.*
import kotlin.random.Random

@Composable
fun CarreraMapaScreen(idCarrera: String) {
    val context = LocalContext.current

    // Mapa de UID a CorredorInfo
    var corredores by remember { mutableStateOf<Map<String, CorredorInfo>>(emptyMap()) }
    val coloresCorredores = remember { mutableStateMapOf<String, BitmapDescriptor>() }

    val dbRef = FirebaseDatabase.getInstance().reference
        .child("carreras")
        .child(idCarrera)
        .child("corredores")

    // Suscripción a Firebase
    LaunchedEffect(Unit) {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nuevos = mutableMapOf<String, CorredorInfo>()
                for (child in snapshot.children) {
                    val lat = child.child("lat").getValue(Double::class.java)
                    val lng = child.child("lng").getValue(Double::class.java)
                    val nombre = child.child("nombre").getValue(String::class.java) ?: "Corredor"
                    val uid = child.key ?: continue

                    if (lat != null && lng != null) {
                        nuevos[uid] = CorredorInfo(nombre, LatLng(lat, lng))
                        if (coloresCorredores[uid] == null) {
                            val colorHue = Random.nextFloat() * 360f
                            coloresCorredores[uid] = BitmapDescriptorFactory.defaultMarker(colorHue)
                        }
                    }
                }
                corredores = nuevos
                Log.d("MapaScreen", "Corredores recibidos: ${corredores.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MapaScreen", "Error al leer corredores: ${error.message}")
            }
        })
    }

    val defaultLatLng = corredores.values.firstOrNull()?.latLng ?: LatLng(-34.9011, -56.1645)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLatLng, 14f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        corredores.forEach { (uid, info) ->
            Marker(
                state = MarkerState(position = info.latLng),
                title = info.nombre,
                icon = coloresCorredores[uid]
            )
        }
    }
}
