package com.example.app_ecosense.models

import com.google.gson.annotations.SerializedName

data class PlantaDetailModelo(
    val id: Int,
    val nom: String,
    val ubicacio: String,
    val imagen_url: String?,
    val sensor_id: Int,
    val sensor_estat: String,
    @SerializedName("humitat_valor")
    val humitat_valor: Float = -1f,
    val humitat_timestamp: String,
    val estat_planta: String
)