package com.example.app_ecosense.models

data class LecturaHumedad(
    val id: Int,
    val sensor_id: Int,
    val valor: Float,
    val timestamp: String
)