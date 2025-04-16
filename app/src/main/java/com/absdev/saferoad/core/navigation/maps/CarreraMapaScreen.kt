package com.absdev.saferoad.core.navigation.maps

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.absdev.saferoad.core.navigation.model.CalidadRed
import com.absdev.saferoad.core.navigation.model.CorredorInfo
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

@Composable
fun CarreraMapaScreen(idCarrera: String) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    var corredores by remember { mutableStateOf<Map<String, CorredorInfo>>(emptyMap()) }
    val coloresCorredores = remember { mutableStateMapOf<String, BitmapDescriptor>() }
    var puntoInicialCarrera by remember { mutableStateOf<LatLng?>(null) }
    var rutaCarrera by remember { mutableStateOf<List<Triple<LatLng, CalidadRed, String>>>(emptyList()) }

    val dbRef = FirebaseDatabase.getInstance().reference
        .child("carreras")
        .child(idCarrera)
        .child("corredores")

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

        try {
            val doc = firestore.collection("carreras").document(idCarrera).get().await()
            val lista = doc.get("ruta") as? List<*>
            val puntos = lista?.mapNotNull {
                val item = it as? Map<*, *> ?: return@mapNotNull null
                val lat = item["lat"] as? Double
                val lng = item["lng"] as? Double
                val calidadStr = item["calidad"] as? String ?: "MEDIA"

                if (lat != null && lng != null) {
                    val calidad = when (calidadStr.uppercase()) {
                        "BUENA" -> CalidadRed.BUENA
                        "MEDIA" -> CalidadRed.MEDIA
                        "MALA" -> CalidadRed.MALA
                        else -> CalidadRed.MEDIA
                    }
                    Triple(LatLng(lat, lng), calidad, calidadStr)
                } else null
            } ?: emptyList()

            if (puntos.isNotEmpty()) {
                puntoInicialCarrera = puntos.first().first
                rutaCarrera = puntos
            }
        } catch (e: Exception) {
            Log.e("MapaScreen", "Error al cargar ruta inicial: ${e.message}")
        }
    }

    val defaultLatLng = corredores.values.firstOrNull()?.latLng
        ?: puntoInicialCarrera
        ?: LatLng(-30.9056, -55.5500)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLatLng, 14f)
    }

    val styleJson = """
        [
          {
            "featureType": "all",
            "elementType": "all",
            "stylers": [
              { "saturation": -100 },
              { "gamma": 0.8 },
              { "lightness": 10 },
              { "visibility": "simplified" }
            ]
          }
        ]
    """.trimIndent()

    val mapStyleOptions = remember { MapStyleOptions(styleJson) }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapStyleOptions = mapStyleOptions),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false
        )
    ) {
        if (rutaCarrera.size >= 2) {
            for (i in 1 until rutaCarrera.size) {
                val (p1, calidad, _) = rutaCarrera[i - 1]
                val (p2, _, _) = rutaCarrera[i]

                val color = when (calidad) {
                    CalidadRed.BUENA -> Color.Green
                    CalidadRed.MEDIA -> Color.Yellow
                    CalidadRed.MALA -> Color.Red
                }

                Polyline(
                    points = listOf(p1, p2),
                    color = color,
                    width = 6f
                )
            }

            Marker(state = MarkerState(position = rutaCarrera.first().first), title = "Inicio")
            Marker(state = MarkerState(position = rutaCarrera.last().first), title = "Fin")
        }

        corredores.forEach { (uid, info) ->
            Marker(
                state = MarkerState(position = info.latLng),
                title = info.nombre,
                icon = coloresCorredores[uid]
            )
        }
    }
}