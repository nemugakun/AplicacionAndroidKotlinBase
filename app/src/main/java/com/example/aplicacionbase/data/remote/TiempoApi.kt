package com.example.aplicacionbase.data.remote

import com.example.aplicacionbase.data.remote.dto.RespuestaTiempoDto
import retrofit2.http.GET
import retrofit2.http.Query

interface TiempoApi {
    // Retrofit genera la implementación de esta interfaz.
    // suspend permite esperar la respuesta sin bloquear la interfaz de Android.
    @GET("v1/forecast")
    suspend fun consultarSemana(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        @Query("timezone") zonaHoraria: String = "auto",
        @Query("past_days") diasPasados: Int = 6,
        @Query("forecast_days") diasPrevision: Int = 7,
        @Query("daily") variables: String = "weather_code"
    ): RespuestaTiempoDto
}

