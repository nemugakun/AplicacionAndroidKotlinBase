package com.example.aplicacionbase.data.remote.dto

import com.google.gson.annotations.SerializedName

// DTO: objeto cuya estructura coincide con la respuesta JSON.
// Los campos son anulables porque una respuesta externa puede estar incompleta.
// Todos tienen valor por defecto: Gson dispone de un constructor sin argumentos.
data class RespuestaTiempoDto(
    @SerializedName("timezone")
    val zonaHoraria: String? = null,

    @SerializedName("daily")
    val dias: DiasTiempoDto? = null
)

data class DiasTiempoDto(
    @SerializedName("time")
    val fechas: List<String?>? = null,

    @SerializedName("weather_code")
    val codigos: List<Int?>? = null
)

