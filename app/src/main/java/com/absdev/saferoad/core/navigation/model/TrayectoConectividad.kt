package com.absdev.saferoad.core.navigation.model

import com.google.android.gms.maps.model.LatLng

data class TrayectoConectividad(
    val latLng: LatLng,
    val calidadRed: CalidadRed
)

enum class CalidadRed {
    BUENA, MEDIA, MALA
}