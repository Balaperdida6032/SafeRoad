package com.absdev.saferoad.core.navigation.Home.CarreraForm

import android.util.Base64
import android.net.Uri
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun UploadImageScreen() {
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var base64Image by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        selectedUri = it
        base64Image = it?.let { uri -> encodeImageToBase64(context, uri) } // Usa la función de CarreraFormScreen
    }

    Column(Modifier.padding(16.dp)) {
        Button(onClick = { launcher.launch("image/*") }) {
            Text("Seleccionar imagen")
        }

        Spacer(modifier = Modifier.height(16.dp))

        base64Image?.let { encoded ->
            Button(onClick = {
                val data = mapOf(
                    "name" to "Carrera desde UploadScreen",
                    "description" to "Subida con Base64",
                    "image" to encoded
                )
                Firebase.firestore.collection("carreras").add(data)
            }) {
                Text("Subir a Firestore")
            }

            Spacer(modifier = Modifier.height(16.dp))

            val imageBitmap = Base64.decode(encoded, Base64.DEFAULT).let { byteArray ->
                BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)?.asImageBitmap()
            }

            imageBitmap?.let {
                Image(bitmap = it, contentDescription = null, modifier = Modifier.size(200.dp))
            }
        }
    }
}
