package com.absdev.saferoad.core.navigation.HomeView

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.absdev.saferoad.core.navigation.model.Carrera
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HomeViewModel:ViewModel() {

    private var db: FirebaseFirestore = Firebase.firestore

    private val _carrera = MutableStateFlow<List<Carrera>>(emptyList())
    val carrera:StateFlow<List<Carrera>> = _carrera

    init {
        getArtist()
    }

    private fun getArtist() {
        viewModelScope.launch {
          val result: List<Carrera> = withContext(Dispatchers.IO){
                getAllArtist()
            }
            _carrera.value = result
        }
    }

    private suspend fun getAllArtist():List<Carrera>{
        return try {
            db.collection("carreras")
                .get()
                .await()
                .documents
                .mapNotNull { snapshot ->
                    snapshot.toObject(Carrera::class.java)
                }
        }catch (e:Exception){
            Log.i("aris", e.toString())
            emptyList()
        }

    }

}