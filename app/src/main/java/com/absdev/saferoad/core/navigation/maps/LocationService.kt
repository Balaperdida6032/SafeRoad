package com.absdev.saferoad.core.navigation.maps

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.absdev.saferoad.R
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                val db = FirebaseDatabase.getInstance().reference
                val firestore = FirebaseFirestore.getInstance()
                val location: Location = locationResult.lastLocation ?: return
                val latLng = LatLng(location.latitude, location.longitude)

                firestore.collection("profile").document(userId)
                    .get()
                    .addOnSuccessListener { doc ->
                        val nombre = doc.getString("name") ?: "Corredor"
                        val carreraActiva = doc.getString("carreraActiva") ?: return@addOnSuccessListener

                        val userLocMap = mapOf(
                            "lat" to location.latitude,
                            "lng" to location.longitude,
                            "timestamp" to System.currentTimeMillis(),
                            "nombre" to nombre
                        )

                        db.child("carreras").child(carreraActiva).child("corredores").child(userId)
                            .setValue(userLocMap)
                    }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }

        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val channelId = "gps_channel"
        val channelName = "Seguimiento GPS"

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("SafeRoad")
            .setContentText("Compartiendo tu ubicación en la carrera")
            .setSmallIcon(R.drawable.saferoadlogo)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 777
    }
}