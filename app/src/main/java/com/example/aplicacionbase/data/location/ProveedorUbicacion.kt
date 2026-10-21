package com.example.aplicacionbase.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.example.aplicacionbase.domain.model.Coordenadas
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class ProveedorUbicacion(contexto: Context) {
    // Conservamos Application, nunca una Activity.
    private val contextoApp = contexto.applicationContext
    private val cliente = LocationServices.getFusedLocationProviderClient(contextoApp)

    fun tienePermiso(): Boolean =
        permisoConcedido(Manifest.permission.ACCESS_COARSE_LOCATION) ||
            permisoConcedido(Manifest.permission.ACCESS_FINE_LOCATION)

    private fun permisoConcedido(permiso: String): Boolean =
        ContextCompat.checkSelfPermission(contextoApp, permiso) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    suspend fun obtenerCoordenadas(): Coordenadas {
        if (!tienePermiso()) throw SecurityException("Falta permiso de ubicación.")

        val gestor = contextoApp.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!LocationManagerCompat.isLocationEnabled(gestor)) {
            throw UbicacionDesactivadaException()
        }

        val disponible = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(contextoApp)
        if (disponible != ConnectionResult.SUCCESS) {
            throw ServicioUbicacionNoDisponibleException()
        }

        val prioridad = if (permisoConcedido(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }
        val solicitud = CurrentLocationRequest.Builder()
            .setPriority(prioridad)
            .setMaxUpdateAgeMillis(60_000L) // Solo admite una posición reciente.
            .setDurationMillis(20_000L)
            .build()

        return suspendCancellableCoroutine { continuacion ->
            val cancelacion = CancellationTokenSource()
            continuacion.invokeOnCancellation { cancelacion.cancel() }
            try {
                cliente.getCurrentLocation(solicitud, cancelacion.token)
                    .addOnSuccessListener { posicion ->
                        if (continuacion.isActive) {
                            if (posicion == null) {
                                continuacion.resumeWithException(UbicacionNoDisponibleException())
                            } else {
                                continuacion.resume(
                                    Coordenadas(posicion.latitude, posicion.longitude)
                                )
                            }
                        }
                    }
                    .addOnFailureListener { error ->
                        if (continuacion.isActive) continuacion.resumeWithException(error)
                    }
                    .addOnCanceledListener { continuacion.cancel() }
            } catch (error: SecurityException) {
                // El permiso podría haberse revocado desde la comprobación anterior.
                if (continuacion.isActive) continuacion.resumeWithException(error)
            }
        }
    }
}

class UbicacionDesactivadaException : Exception()
class UbicacionNoDisponibleException : Exception()
class ServicioUbicacionNoDisponibleException : Exception()

