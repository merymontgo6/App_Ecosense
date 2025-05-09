package com.example.app_ecosense.models

data class PlantaDetailModelo(
    val id: Int,
    val nom: String,
    val descripcio: String,
    val humitat_optim: Int,
    val estat: String,
    val imagen_url: String?,
)