package com.absdev.saferoad.core.navigation.Home.CarreraDetail

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class InscribirseViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    fun inscribirseACarrera(carreraId: String, userId: String, onResult: (Boolean, String) -> Unit) {
        val carreraRef = db.collection("carreras").document(carreraId)

        carreraRef.get().addOnSuccessListener { snapshot ->
            val hasLimit = snapshot.getBoolean("hasLimit") ?: false
            val limit = snapshot.getLong("limit")?.toInt() ?: 0

            val inscripcionesRef = carreraRef.collection("inscripciones")

            inscripcionesRef.get().addOnSuccessListener { inscripciones ->
                val cantidadActual = inscripciones.size()

                if (hasLimit && cantidadActual >= limit) {
                    onResult(false, "Límite de inscripciones alcanzado ❌")
                } else {
                    inscripcionesRef.document(userId).set(mapOf("userId" to userId))
                        .addOnSuccessListener {
                            onResult(true, "Inscripción exitosa ✅")
                        }
                        .addOnFailureListener {
                            onResult(false, "Error al inscribirse")
                        }
                }
            }
        }.addOnFailureListener {
            onResult(false, "No se pudo cargar la carrera")
        }
    }
}