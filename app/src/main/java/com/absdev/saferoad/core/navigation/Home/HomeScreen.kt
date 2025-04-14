package com.absdev.saferoad.core.navigation.HomeScreen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import coil.compose.AsyncImage
import com.absdev.saferoad.core.navigation.HomeView.HomeViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController
import com.absdev.saferoad.core.navigation.model.Carrera
import com.absdev.saferoad.core.navigation.navigation.CarreraDetailScreen
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun HomeScreen(
    navController : NavController,
    viewModel: HomeViewModel = HomeViewModel()) {

    val carrera: State<List<Carrera>> = viewModel.carrera.collectAsState()

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Black,
            darkIcons = false // esto es clave para que los íconos sean blancos
        )
    }

    Column(Modifier
        .fillMaxSize()
        .background(Color.Black)
        .statusBarsPadding()) {
        Text("Carreras Disponibles",
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
        Box(modifier = Modifier.fillMaxSize()) {
            // Imagen con degradado desde mitad hacia abajo
            AsyncImage(
                model = carrera.image,
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

            // Contenido (nombre, descripción)
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
