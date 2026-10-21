package com.example.aplicacionbase.config

import com.example.aplicacionbase.domain.model.UbicacionTiempo
import java.time.ZoneId

object ConfiguracionTiempo {
    // Ejemplo configurable. Cambia JUNTOS el nombre, las coordenadas y la zona.
    val ubicacion = UbicacionTiempo(
        nombre = "Madrid",
        latitud = 40.4168,
        longitud = -3.7038,
        zonaHoraria = ZoneId.of("Europe/Madrid")
    )
}

