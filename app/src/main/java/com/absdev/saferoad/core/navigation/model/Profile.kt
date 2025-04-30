package com.absdev.saferoad.core.navigation.model

data class Profile (
    val uid: String? = null,
    val name:String? = null,
    val birthDate: String? = null,
    val email:String? = null,
    val password:String? = null,
    val role:String? = "user",
    val pesoKg: Float? = null
)