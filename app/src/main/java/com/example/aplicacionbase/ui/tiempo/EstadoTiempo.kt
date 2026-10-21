package com.example.aplicacionbase.ui.tiempo

import com.example.aplicacionbase.domain.model.SemanaTiempo

sealed interface EstadoTiempo {
    data object EsperandoPermiso : EstadoTiempo
    data object ObteniendoUbicacion : EstadoTiempo
    data object Cargando : EstadoTiempo
    data class Contenido(val semana: SemanaTiempo) : EstadoTiempo
    data class Error(val tipo: TipoErrorTiempo) : EstadoTiempo
}

enum class TipoErrorTiempo {
    SIN_PERMISO,
    UBICACION_DESACTIVADA,
    UBICACION_NO_DISPONIBLE,
    SERVICIO_UBICACION_NO_DISPONIBLE,
    RED,
    SERVIDOR,
    LIMITE_DE_PETICIONES,
    RESPUESTA_INVALIDA,
    DESCONOCIDO
}

