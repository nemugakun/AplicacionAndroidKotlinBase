package com.example.aplicacionbase.domain.model

import java.time.LocalDate

data class TiempoDia(
    val fecha: LocalDate,
    // Es un identificador meteorológico WMO, no un texto en inglés.
    // null significa que el proveedor no dispone del dato para esa fecha.
    val codigoWmo: Int?
)

