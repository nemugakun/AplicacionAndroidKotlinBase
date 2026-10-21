package com.example.aplicacionbase.data.repository

import com.example.aplicacionbase.data.remote.TiempoApi
import com.example.aplicacionbase.domain.model.Coordenadas
import com.example.aplicacionbase.domain.model.SemanaTiempo
import com.example.aplicacionbase.domain.model.TiempoDia
import com.example.aplicacionbase.domain.model.UbicacionTiempo
import java.time.Clock
import java.time.DateTimeException
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

class RepositorioTiempo(
    private val api: TiempoApi,
    private val reloj: Clock = Clock.systemUTC()
) {
    suspend fun obtenerSemana(coordenadas: Coordenadas): SemanaTiempo {
        // Pedimos 6 días pasados y hoy + 6 futuros: contienen la semana actual.
        // timezone=auto permite conocer la zona del lugar con esta misma petición.
        val respuesta = api.consultarSemana(
            latitud = coordenadas.latitud,
            longitud = coordenadas.longitud
        )
        val zona = try {
            ZoneId.of(
                respuesta.zonaHoraria
                    ?: throw RespuestaTiempoInvalidaException("Falta timezone.")
            )
        } catch (error: DateTimeException) {
            throw RespuestaTiempoInvalidaException("Zona horaria no válida.", error)
        }
        val hoy = LocalDate.now(reloj.withZone(zona))
        val inicio = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        val datos = respuesta.dias
            ?: throw RespuestaTiempoInvalidaException("Falta daily.")
        val fechas = datos.fechas
            ?: throw RespuestaTiempoInvalidaException("Falta daily.time.")
        val codigos = datos.codigos
            ?: throw RespuestaTiempoInvalidaException("Falta daily.weather_code.")
        if (fechas.size < 7 || fechas.size != codigos.size) {
            throw RespuestaTiempoInvalidaException("Arrays incompletos o de distinta longitud.")
        }

        val diasRecibidos = fechas.mapIndexed { indice, texto ->
            val fecha = try {
                LocalDate.parse(
                    texto ?: throw RespuestaTiempoInvalidaException("Fecha nula.")
                )
            } catch (error: DateTimeException) {
                throw RespuestaTiempoInvalidaException("Fecha no válida.", error)
            }
            TiempoDia(fecha, codigos[indice])
        }
        if (diasRecibidos.zipWithNext().any { (a, b) -> b.fecha != a.fecha.plusDays(1) }) {
            throw RespuestaTiempoInvalidaException("Fechas duplicadas o desordenadas.")
        }

        val porFecha = diasRecibidos.associateBy { it.fecha }
        val semana = (0L..6L).map { desplazamiento ->
            porFecha[inicio.plusDays(desplazamiento)]
                ?: throw RespuestaTiempoInvalidaException("Falta un día de la semana.")
        }
        return SemanaTiempo(
            ubicacion = UbicacionTiempo(
                nombre = "tu ubicación",
                latitud = coordenadas.latitud,
                longitud = coordenadas.longitud,
                zonaHoraria = zona
            ),
            fechaConsulta = hoy,
            dias = semana
        )
    }
}

class RespuestaTiempoInvalidaException(
    mensaje: String,
    causa: Throwable? = null
) : Exception(mensaje, causa)
