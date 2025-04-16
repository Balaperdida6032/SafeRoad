package com.absdev.saferoad.core.navigation.Home.CarreraForm

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.absdev.saferoad.core.navigation.model.Carrera
import com.absdev.saferoad.core.navigation.navigation.DefinirRutaCarrera
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

@Composable
fun CarreraFormScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageBase64 by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var hasLimit by remember { mutableStateOf(false) }
    var limit by remember { mutableStateOf("") } // como texto, luego se convierte a Int

    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        selectedUri = it
        imageBase64 = it?.let { uri -> encodeImageToBase64(context, uri) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("¿Limitar corredores?", color = Color.White)
            Switch(checked = hasLimit, onCheckedChange = { hasLimit = it })
        }

        if (hasLimit) {
            Spacer(modifier = Modifier.height(12.dp))
            TextField(
                value = limit,
                onValueChange = { limit = it },
                label = { Text("Límite de corredores") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = { launcher.launch("image/*") }) {
            Text("Seleccionar imagen")
        }

        Spacer(modifier = Modifier.height(12.dp))

        imageBase64?.let {
            val imageBitmap = Base64.decode(it, Base64.DEFAULT).let { byteArray ->
                BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)?.asImageBitmap()
            }

            imageBitmap?.let { bitmap ->
                Image(bitmap = bitmap, contentDescription = null, modifier = Modifier.size(200.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                loading = true
                val carreraId = db.collection("carreras").document().id
                val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

                val nuevaCarrera = Carrera(
                    id = carreraId,
                    name = name,
                    description = description,
                    image = imageBase64.orEmpty(),
                    userId = userId,
                    hasLimit = hasLimit,
                    limit = if (hasLimit) limit.toIntOrNull() else null
                )

                db.collection("carreras").document(carreraId)
                    .set(nuevaCarrera)
                    .addOnSuccessListener {
                        Log.i("Firestore", "Carrera creada con éxito")
                        navController.navigate(DefinirRutaCarrera(carreraId))
                    }
                    .addOnFailureListener {
                        Log.e("Firestore", "Error al crear carrera", it)
                    }
                    .addOnCompleteListener {
                        loading = false
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading && !name.isBlank() && !description.isBlank() && !imageBase64.isNullOrEmpty()
        ) {
            Text(text = if (loading) "Guardando..." else "Crear carrera")
        }
    }
}

fun encodeImageToBase64(context: Context, uri: Uri): String? {
    val bitmap: Bitmap = if (Build.VERSION.SDK_INT < 28) {
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    } else {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    }

    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}