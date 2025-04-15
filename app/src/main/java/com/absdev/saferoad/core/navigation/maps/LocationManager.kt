package com.absdev.saferoad.core.navigation.maps

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LocationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val database = FirebaseDatabase.getInstance().reference

    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    @SuppressLint("MissingPermission")
    fun updateUserLocation(idCarrera: String) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val userLocation = mapOf(
                        "lat" to it.latitude,
                        "lng" to it.longitude,
                        "timestamp" to System.currentTimeMillis()
                    )

                    userId?.let { uid ->
                        database.child("carreras").child(idCarrera).child("corredores").child(uid)
                            .setValue(userLocation)
                    }
                }
            }
    }
}