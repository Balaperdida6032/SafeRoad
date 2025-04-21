package com.absdev.saferoad.core.navigation.Home.CarreraDetail

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.absdev.saferoad.core.navigation.model.Carrera
import com.absdev.saferoad.core.navigation.model.CalidadRed
import com.absdev.saferoad.core.navigation.navigation.CarreraMapa
import com.absdev.saferoad.core.navigation.navigation.EditarCarrera
import com.absdev.saferoad.ui.theme.GreenLogo
import com.absdev.saferoad.ui.theme.RedDetener
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun CarreraDetailScreen(carrera: Carrera, navController: NavController) {
    var showConfigButton by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var carreraActualizada by remember { mutableStateOf<Carrera?>(null) }
    var showConfirmDelete by remember { mutableStateOf(false) }
    var carreraIniciada by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()
    val inscribirseViewModel = remember { InscribirseViewModel() }
    val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    var mensajeInscripcion by remember { mutableStateOf<String?>(null) }

    val carreraVisible = carreraActualizada ?: carrera
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    val imageBitmap = carreraVisible.image?.let { base64 ->
        try {
            val imageBytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect

        carrera.id?.let { idCarrera ->
            val carreraRef = db.collection("carreras").document(idCarrera)
            val perfilRef = db.collection("profile").document(userId)

            val carreraDoc = carreraRef.get().await()
            val perfilDoc = perfilRef.get().await()

            val isStarted = carreraDoc.getBoolean("isStarted") ?: false
            val isAdmin = perfilDoc.getString("role") == "admin"
            val isCreator = carrera.userId == userId
            showConfigButton = isAdmin && isCreator

            carreraActualizada = carreraDoc.toObject(Carrera::class.java)
            carreraIniciada = isStarted
        }
    }

    var inscriptosActuales by remember { mutableStateOf(0) }
    val tieneLimite = carreraVisible.hasLimit == true
    val limite = carreraVisible.limit ?: 0
    val actualizarInscriptosTrigger = remember { mutableStateOf(0) }

    LaunchedEffect(carreraVisible, actualizarInscriptosTrigger.value) {
        carreraVisible.id?.let { id ->
            val snapshot = db.collection("carreras").document(id)
                .collection("inscripciones").get().await()
            inscriptosActuales = snapshot.size()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
    ) {
        val listState = rememberLazyListState()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight * 0.5f)
        ) {
            LazyRow(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    imageBitmap?.let {
                        Box(
                            modifier = Modifier
                                .fillParentMaxHeight()
                                .width(LocalConfiguration.current.screenWidthDp.dp)
                        ) {
                            Image(
                                bitmap = it,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            // Degradado negro desde abajo hacia arriba
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.8f),
                                                Color.Transparent
                                            ),
                                            startY = Float.POSITIVE_INFINITY,
                                            endY = 0f
                                        )
                                    )
                            )
                        }
                    }
                }

                item {
                    val puntosRuta = carreraVisible.ruta?.mapNotNull { punto ->
                        val lat = punto["lat"] as? Double
                        val lng = punto["lng"] as? Double
                        val calidadStr = punto["calidad"] as? String
                        if (lat != null && lng != null && calidadStr != null) {
                            val calidad = when (calidadStr) {
                                "BUENA" -> CalidadRed.BUENA
                                "MEDIA" -> CalidadRed.MEDIA
                                "MALA" -> CalidadRed.MALA
                                else -> CalidadRed.MEDIA
                            }
                            Triple(LatLng(lat, lng), calidad, calidadStr)
                        } else null
                    } ?: emptyList()

                    if (puntosRuta.size >= 2) {
                        val cameraPositionState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(puntosRuta.first().first, 15f)
                        }

                        val mapStyleOptions = remember {
                            MapStyleOptions(
                                """[{"featureType":"all","elementType":"all","stylers":[{"saturation":-100},{"gamma":0.8},{"lightness":10},{"visibility":"simplified"}]}]"""
                            )
                        }

                        GoogleMap(
                            modifier = Modifier
                                .fillParentMaxHeight()
                                .width(LocalConfiguration.current.screenWidthDp.dp),
                            cameraPositionState = cameraPositionState,
                            properties = MapProperties(mapStyleOptions = mapStyleOptions),
                            uiSettings = MapUiSettings(
                                zoomControlsEnabled = false,
                                mapToolbarEnabled = false,
                                myLocationButtonEnabled = false
                            )
                        ) {
                            for (i in 1 until puntosRuta.size) {
                                val (p1, calidad, _) = puntosRuta[i - 1]
                                val (p2, _, _) = puntosRuta[i]
                                val color = when (calidad) {
                                    CalidadRed.BUENA -> Color.Green
                                    CalidadRed.MEDIA -> Color.Yellow
                                    CalidadRed.MALA -> Color.Red
                                }
                                Polyline(points = listOf(p1, p2), color = color, width = 6f)
                            }

                            Marker(state = MarkerState(puntosRuta.first().first), title = "Inicio")
                            Marker(state = MarkerState(puntosRuta.last().first), title = "Fin")
                        }
                    }
                }
            }

            // Nombre superpuesto SOLO cuando la imagen está visible
            if (listState.firstVisibleItemIndex == 0) {
                Text(
                    text = carreraVisible.name.orEmpty(),
                    color = Color.White,
                    fontSize = 50.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )
            }

            // Botones superpuestos
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 16.dp)
                    .align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.3f), shape = CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Botón central Iniciar / Detener
                if (showConfigButton) {
                    val buttonColor = if (!carreraIniciada) GreenLogo else RedDetener
                    val buttonText = if (!carreraIniciada) "Iniciar" else "Detener"

                    Button(
                        onClick = {
                            carreraVisible.id?.let { id ->
                                db.collection("carreras").document(id)
                                    .update("isStarted", !carreraIniciada)
                                    .addOnSuccessListener {
                                        carreraIniciada = !carreraIniciada
                                    }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(buttonText, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Menú de configuración
                if (showConfigButton) {
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.3f), shape = CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Settings,
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

        }


       /* Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = carreraVisible.name.orEmpty(),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp)
        )*/

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = carreraVisible.description.orEmpty(),
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Icono cupos",
                tint = Color.LightGray,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = if (tieneLimite) {
                    "$inscriptosActuales / $limite"
                } else {
                    "Sin límite"
                },
                color = Color.LightGray,
                fontSize = 14.sp
            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                carreraVisible.id?.let { carreraId ->
                    inscribirseViewModel.inscribirseACarrera(carreraId, userId) { success, mensaje ->
                        mensajeInscripcion = mensaje
                        if (success) actualizarInscriptosTrigger.value++
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenLogo),
            enabled = !tieneLimite || (limite - inscriptosActuales > 0)
        ) {
            Text("Inscribirme", color = Color.White)
        }

        mensajeInscripcion?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = Color.White, modifier = Modifier.padding(horizontal = 16.dp))
        }

        if (carreraIniciada) {
            Button(
                onClick = {
                    carreraVisible.id?.let {
                        navController.navigate(CarreraMapa(it))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenLogo)
            ) {
                Text("Ver carrera", color = Color.White)
            }
        }

    }

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("¿Eliminar carrera?", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que deseas eliminar esta carrera? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        carreraVisible.id?.let { carreraId ->
                            val carreraRef = db.collection("carreras").document(carreraId)
                            val inscripcionesRef = carreraRef.collection("inscripciones")

                            inscripcionesRef.get().addOnSuccessListener { snapshot ->
                                val batch = db.batch()
                                for (doc in snapshot.documents) batch.delete(doc.reference)
                                batch.commit().addOnSuccessListener {
                                    carreraRef.delete().addOnSuccessListener {
                                        navController.popBackStack()
                                    }
                                }
                            }
                        }
                        showConfirmDelete = false
                    }
                ) { Text("Eliminar", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) {
                    Text("Cancelar")
                }
            },
            containerColor = Color.White
        )
    }
}
