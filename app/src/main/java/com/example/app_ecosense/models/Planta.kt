package com.example.app_ecosense.models

data class Planta(
    val id: Int,
    val nom: String,
    val ubicacio: String,
    val sensor_id: Int,
    val usuari_id: Int?
)