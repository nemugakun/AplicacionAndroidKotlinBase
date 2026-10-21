package com.example.aplicacionbase.domain.model

import java.time.ZoneId

data class UbicacionTiempo(
    val nombre: String,
    val latitud: Double,
    val longitud: Double,
    val zonaHoraria: ZoneId
)

