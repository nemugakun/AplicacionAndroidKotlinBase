package com.example.aplicacionbase.ui.tiempo

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacionbase.data.location.ProveedorUbicacion
import com.example.aplicacionbase.data.location.ServicioUbicacionNoDisponibleException
import com.example.aplicacionbase.data.location.UbicacionDesactivadaException
import com.example.aplicacionbase.data.location.UbicacionNoDisponibleException
import com.example.aplicacionbase.data.remote.ClienteTiempo
import com.example.aplicacionbase.data.repository.RepositorioTiempo
import com.example.aplicacionbase.data.repository.RespuestaTiempoInvalidaException
import com.example.aplicacionbase.domain.model.SemanaTiempo
import com.google.android.gms.common.api.ApiException
import com.google.gson.JsonParseException
import java.io.IOException
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class TiempoViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {
    private val proveedor = ProveedorUbicacion(aplicacion)
    private val repositorio = RepositorioTiempo(ClienteTiempo.api)
    private val _estado = MutableStateFlow<EstadoTiempo>(EstadoTiempo.EsperandoPermiso)
    val estado: StateFlow<EstadoTiempo> = _estado.asStateFlow()

    private var peticion: Job? = null
    private var ultimaSemana: SemanaTiempo? = null
    private var ultimaFechaIntentada: LocalDate? = null

    fun alEntrar() {
        if (proveedor.tienePermiso()) actualizar() else alDenegarPermiso()
    }

    fun alDenegarPermiso() {
        peticion?.cancel()
        ultimaSemana = null
        _estado.value = EstadoTiempo.EsperandoPermiso
    }

    fun alSalir() {
        // Cancela la lectura de posición y cualquier consulta Retrofit pendiente.
        peticion?.cancel()
        peticion = null
        _estado.value = ultimaSemana?.let { EstadoTiempo.Contenido(it) }
            ?: EstadoTiempo.EsperandoPermiso
    }

    fun actualizarSiCambioElDia() {
        val anterior = ultimaSemana ?: return
        val hoy = LocalDate.now(anterior.ubicacion.zonaHoraria)
        if (hoy != anterior.fechaConsulta && hoy != ultimaFechaIntentada) actualizar()
    }

    fun actualizar() {
        if (peticion?.isActive == true) return
        if (!proveedor.tienePermiso()) {
            alDenegarPermiso()
            return
        }
        _estado.value = EstadoTiempo.ObteniendoUbicacion
        ultimaFechaIntentada = ultimaSemana?.let { LocalDate.now(it.ubicacion.zonaHoraria) }
        peticion = viewModelScope.launch {
            try {
                val coordenadas = proveedor.obtenerCoordenadas()
                _estado.value = EstadoTiempo.Cargando
                val semana = repositorio.obtenerSemana(coordenadas)
                ultimaSemana = semana
                ultimaFechaIntentada = semana.fechaConsulta
                _estado.value = EstadoTiempo.Contenido(semana)
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                Log.e("TiempoCompose", "No se pudo completar la consulta.", error)
                val tipo = when (error) {
                    is SecurityException -> TipoErrorTiempo.SIN_PERMISO
                    is UbicacionDesactivadaException -> TipoErrorTiempo.UBICACION_DESACTIVADA
                    is UbicacionNoDisponibleException -> TipoErrorTiempo.UBICACION_NO_DISPONIBLE
                    is ServicioUbicacionNoDisponibleException,
                    is ApiException -> TipoErrorTiempo.SERVICIO_UBICACION_NO_DISPONIBLE
                    is HttpException -> {
                        if (error.code() == 429) TipoErrorTiempo.LIMITE_DE_PETICIONES
                        else TipoErrorTiempo.SERVIDOR
                    }
                    is RespuestaTiempoInvalidaException,
                    is JsonParseException -> TipoErrorTiempo.RESPUESTA_INVALIDA
                    is IOException -> TipoErrorTiempo.RED
                    else -> TipoErrorTiempo.DESCONOCIDO
                }
                _estado.value = EstadoTiempo.Error(tipo)
            }
        }
    }
}

