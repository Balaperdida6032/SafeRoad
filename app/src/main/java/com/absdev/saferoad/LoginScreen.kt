package com.absdev.saferoad

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(auth: FirebaseAuth/*, navigateToHome:() -> Unit */) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize(),horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.weight(1f))
        Text(text = "LOGIN SCREEN", fontSize = 25.sp)
        Spacer(modifier = Modifier.weight(1f))

        //Campo de Usuario
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") })

        Spacer(modifier = Modifier.weight(1f))

        //Campo de contrasenia
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Default.Favorite else Icons.Default.FavoriteBorder
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = icon, contentDescription = "Toggle password visibility")
                }
            }
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = { auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{ task ->
            if (task.isSuccessful){
                //Navegar
                Log.i("agu", "LOGIN OK")
            }else {
                //Error
                Log.i("agu", "NO LOGIN OK")
            }
        } /*navigateToHome(username)*/ }) {
            Text(text = "Confirmar")
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}