package com.absdev.saferoad.core.navigation.Home.CarreraDetail

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.absdev.saferoad.core.navigation.model.Carrera
import com.absdev.saferoad.ui.theme.GreenLogo
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

@Composable
fun EditarCarreraScreen(carrera: Carrera, navController: NavController) {
    var name by remember { mutableStateOf(carrera.name.orEmpty()) }
    var description by remember { mutableStateOf(carrera.description.orEmpty()) }
    var imageBase64 by remember { mutableStateOf(carrera.image) }

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

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
        Text("Editar carrera", color = Color.White, fontSize = 24.sp)

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            singleLine = true,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción") },
            singleLine = true,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = { launcher.launch("image/*") },
            colors = ButtonDefaults.buttonColors(containerColor = GreenLogo)) {
            Text("Seleccionar nueva imagen", color = Color.White)
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
                val updated = mapOf(
                    "name" to name,
                    "description" to description,
                    "image" to imageBase64.orEmpty()
                )
                db.collection("carreras").document(carrera.id!!)
                    .update(updated)
                    .addOnSuccessListener {
                        navController.popBackStack()
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = GreenLogo)
        ) {
            Text("Guardar cambios",color = Color.White)
        }
    }
}

fun encodeImageToBase64(context: Context, uri: Uri): String? {
    val bitmap = if (Build.VERSION.SDK_INT < 28) {
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
