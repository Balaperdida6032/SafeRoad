package com.absdev.saferoad.core.navigation.Profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.absdev.saferoad.core.navigation.model.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val snapshot = db.collection("profile")
                    .document(userId)
                    .get()
                    .await()

                val userProfile = snapshot.toObject(Profile::class.java)
                _profile.value = userProfile
            } catch (e: Exception) {
                e.printStackTrace()
                _profile.value = null
            }
        }
    }
}