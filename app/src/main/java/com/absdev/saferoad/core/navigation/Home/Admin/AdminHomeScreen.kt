package com.absdev.saferoad.core.navigation.Home.Admin.AdminHomeScreen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.absdev.saferoad.core.navigation.HomeView.HomeViewModel
import com.absdev.saferoad.core.navigation.model.Carrera
import com.absdev.saferoad.core.navigation.navigation.CarreraDetailScreen
import com.absdev.saferoad.core.navigation.navigation.CarreraForm
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import android.util.Base64
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

@Composable
fun AdminHomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = HomeViewModel()) {
    val carrera: State<List<Carrera>> = viewModel.carrera.collectAsState()

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Black,
            darkIcons = false
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                "Carreras Disponibles Admin mode",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow {
                items(carrera.value) { item ->
                    CarreraItem(carrera = item) {
                        val currentEntry = navController.currentBackStackEntry
                        if (currentEntry != null) {
                            currentEntry.savedStateHandle.set("carrera", item)
                            navController.navigate(CarreraDetailScreen)
                        } else {
                            Log.e("NAV", "currentBackStackEntry es null")
                        }

                    }
                }

            }
            Spacer(modifier = Modifier.weight(1f))
        }

        FloatingActionButton(
            onClick = {
                Log.i("FAB", "Botón flotante presionado")
                navController.navigate(CarreraForm::class.qualifiedName!!)
            },
            containerColor = Color.White,
            contentColor = Color.Black,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(56.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Agregar")
        }
    }
}

@Composable
fun CarreraItem(carrera: Carrera, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .size(width = 280.dp, height = 450.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val imageBitmap = remember(carrera.image) {
                decodeBase64ToImage(carrera.image ?: "")
            }
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Carrera image",
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black),
                                    startY = size.height * 0.5f,
                                    endY = size.height
                                )
                            )
                        },
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Imagen no disponible", color = Color.White)
                }
            }


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = carrera.name.orEmpty(),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = carrera.description.orEmpty(),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    maxLines = 3
                )
            }
        }
    }
}

fun decodeBase64ToImage(base64: String): ImageBitmap? {
    return try {
        val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        bitmap?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}
