package com.example.aplicacionbase.domain.model

import java.time.LocalDate

data class SemanaTiempo(
    val ubicacion: UbicacionTiempo,
    val fechaConsulta: LocalDate,
    val dias: List<TiempoDia>
) {
    init {
        require(dias.size == 7) { "Una semana debe contener siete días." }
    }

    val inicio: LocalDate get() = dias.first().fecha
    val fin: LocalDate get() = dias.last().fecha
}

