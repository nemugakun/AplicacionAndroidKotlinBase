package com.example.aplicacionbase.domain.model

data class Coordenadas(val latitud: Double, val longitud: Double) {
    init {
        require(latitud.isFinite() && latitud in -90.0..90.0)
        require(longitud.isFinite() && longitud in -180.0..180.0)
    }
}

