package com.absdev.saferoad.core.navigation.navigation

import kotlinx.serialization.Serializable

@Serializable
object Splash

@Serializable
object Welcome

@Serializable
object Login

@Serializable
object Sign

@Serializable
object Home

@Serializable
object AdminHome

@Serializable
object Profile

@Serializable
object CarreraForm

@Serializable
object CarreraDetailScreen

@Serializable
object UploadImage

@Serializable
object EditarCarrera

@Serializable
object EditarPerfil

@Serializable
data class CarreraMapa(val carreraId: String)

@Serializable
data class ParticipanteCarreraStart(val carreraId: String)

@Serializable
data class DefinirRutaCarrera(val carreraId: String)

