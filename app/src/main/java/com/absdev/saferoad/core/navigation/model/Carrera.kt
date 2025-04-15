package com.absdev.saferoad.core.navigation.model

import java.io.Serializable

data class Carrera (
    val id:String? = null,
    val name:String? = null,
    val description:String? = null,
    val image:String? = null,
    val userId:String? = null,
    val hasLimit: Boolean = false,
    val limit: Int? = null
): Serializable

