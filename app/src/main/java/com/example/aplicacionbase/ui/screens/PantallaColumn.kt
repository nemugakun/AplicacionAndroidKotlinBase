package com.example.aplicacionbase.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.LifecycleStartEffect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aplicacionbase.R
import com.example.aplicacionbase.domain.model.SemanaTiempo
import com.example.aplicacionbase.domain.model.TiempoDia
import com.example.aplicacionbase.ui.tiempo.EstadoTiempo
import com.example.aplicacionbase.ui.tiempo.PresentacionCielo
import com.example.aplicacionbase.ui.tiempo.TiempoViewModel
import com.example.aplicacionbase.ui.tiempo.TipoErrorTiempo
import com.example.aplicacionbase.ui.tiempo.presentacionCielo
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.milliseconds
import androidx.core.net.toUri

private val idiomaTiempo = Locale.Builder().setLanguage("es").setRegion("ES").build()//Locale("es", "ES")
private val formatoFechaTiempo = DateTimeFormatter.ofPattern("dd/MM/yyyy", idiomaTiempo)

@Composable
fun PantallaColumn(
    modifier: Modifier = Modifier,
    modelo: TiempoViewModel = viewModel()
) {
    val estado by modelo.estado.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    val propietario = LocalLifecycleOwner.current
    var solicitudesRealizadas by rememberSaveable { mutableIntStateOf(0) }

    val permisos = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    val solicitud = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        solicitudesRealizadas++
        val concedido =
            resultado[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
                    resultado[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (concedido) modelo.actualizar() else modelo.alDenegarPermiso()
    }

    val actividad = contexto.encontrarActivity()
    val sePuedeSolicitar = solicitudesRealizadas == 0 ||
            actividad == null ||
            permisos.any { ActivityCompat.shouldShowRequestPermissionRationale(actividad, it) }

    val abrirAjustesApp: () -> Unit = {
        contexto.startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                ("package:" + contexto.packageName).toUri()
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
    val pedirPermiso: () -> Unit = {
        if (sePuedeSolicitar) {
            solicitud.launch(permisos)
        } else {
            abrirAjustesApp()
        }
    }

    // Se ejecuta al entrar o volver de ajustes, no en cada recomposición.
    LifecycleStartEffect(modelo) {
        modelo.alEntrar()
        onStopOrDispose { modelo.alSalir() }
    }

    LaunchedEffect(propietario, modelo) {
        propietario.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (isActive) {
                modelo.actualizarSiCambioElDia()
                delay(60_000L.milliseconds)
            }
        }
    }

    when (val actual = estado) {
        EstadoTiempo.EsperandoPermiso -> {
            PanelPermisoUbicacion(
                onSolicitar = pedirPermiso,
                abrirAjustes = !sePuedeSolicitar,
                modifier = modifier
            )
        }
        EstadoTiempo.ObteniendoUbicacion -> {
            IndicadorTiempo(R.string.tiempo_obteniendo_ubicacion, modifier)
        }
        EstadoTiempo.Cargando -> {
            IndicadorTiempo(R.string.tiempo_cargando, modifier)
        }
        is EstadoTiempo.Error -> {
            if (actual.tipo == TipoErrorTiempo.SIN_PERMISO) {
                PanelPermisoUbicacion(
                    onSolicitar = pedirPermiso,
                    abrirAjustes = !sePuedeSolicitar,
                    modifier = modifier
                )
            } else {
                val mensaje = when (actual.tipo) {
                    TipoErrorTiempo.UBICACION_DESACTIVADA -> R.string.tiempo_ubicacion_desactivada
                    TipoErrorTiempo.UBICACION_NO_DISPONIBLE -> R.string.tiempo_ubicacion_no_disponible
                    TipoErrorTiempo.SERVICIO_UBICACION_NO_DISPONIBLE -> R.string.tiempo_servicio_ubicacion
                    TipoErrorTiempo.RED -> R.string.tiempo_error_red
                    TipoErrorTiempo.SERVIDOR -> R.string.tiempo_error_servidor
                    TipoErrorTiempo.LIMITE_DE_PETICIONES -> R.string.tiempo_error_limite
                    TipoErrorTiempo.RESPUESTA_INVALIDA -> R.string.tiempo_error_datos
                    else -> R.string.tiempo_error_general
                }
                Column(
                    modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(stringResource(mensaje))
                    Button(onClick = modelo::actualizar) {
                        Text(stringResource(R.string.tiempo_reintentar))
                    }
                    if (actual.tipo == TipoErrorTiempo.UBICACION_DESACTIVADA) {
                        TextButton(onClick = {
                            contexto.startActivity(
                                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }) {
                            Text(stringResource(R.string.tiempo_abrir_ubicacion))
                        }
                    }
                }
            }
        }
        is EstadoTiempo.Contenido -> {
            ColumnaSemana(
                semana = actual.semana,
                onActualizar = modelo::actualizar,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun PanelPermisoUbicacion(
    onSolicitar: () -> Unit,
    abrirAjustes: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.tiempo_titulo_ubicacion),
            style = MaterialTheme.typography.titleLarge
        )
        Text(stringResource(R.string.tiempo_motivo_permiso))
        if (abrirAjustes) {
            Text(stringResource(R.string.tiempo_permiso_ajustes))
        }
        Button(onClick = onSolicitar) {
            Text(
                stringResource(
                    if (abrirAjustes) R.string.tiempo_abrir_ajustes
                    else R.string.tiempo_usar_ubicacion
                )
            )
        }
    }
}

@Composable
private fun IndicadorTiempo(texto: Int, modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator()
            Text(stringResource(texto))
        }
    }
}

private tailrec fun Context.encontrarActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.encontrarActivity()
    else -> null
}

@Composable
private fun ColumnaSemana(
    semana: SemanaTiempo,
    onActualizar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enlaces = LocalUriHandler.current

    // Siete elementos conocidos: una Column con scroll es suficiente.
    // Los márgenes exteriores y las barras los aporta el Scaffold existente.
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.tiempo_localidad, semana.ubicacion.nombre),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge
            )
            Button(onClick = onActualizar) {
                Text(stringResource(R.string.tiempo_actualizar))
            }
        }

        Text(
            text = stringResource(
                R.string.tiempo_coordenadas,
                String.format(idiomaTiempo, "%.3f", semana.ubicacion.latitud),
                String.format(idiomaTiempo, "%.3f", semana.ubicacion.longitud)
            ),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = stringResource(
                R.string.tiempo_rango,
                semana.inicio.format(formatoFechaTiempo),
                semana.fin.format(formatoFechaTiempo)
            ),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = stringResource(R.string.tiempo_explicacion),
            style = MaterialTheme.typography.bodySmall
        )

        semana.dias.forEach { dia ->
            FilaTiempo(dia = dia, hoy = semana.fechaConsulta)
        }

        Text(
            text = stringResource(R.string.tiempo_atribucion),
            style = MaterialTheme.typography.bodySmall
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { enlaces.openUri("https://open-meteo.com/") }) {
                Text(stringResource(R.string.tiempo_fuente))
            }
            TextButton(onClick = {
                enlaces.openUri("https://creativecommons.org/licenses/by/4.0/")
            }) {
                Text(stringResource(R.string.tiempo_licencia))
            }
        }
    }
}

@Composable
private fun FilaTiempo(dia: TiempoDia, hoy: LocalDate) {
    val cielo = presentacionCielo(dia.codigoWmo)
    val esHoy = dia.fecha == hoy
    val nombreDia = dia.fecha.dayOfWeek
        .getDisplayName(TextStyle.FULL, idiomaTiempo)
        .replaceFirstChar { it.titlecase(idiomaTiempo) }
    val etiquetaFecha = when {
        esHoy -> R.string.tiempo_fecha_hoy
        dia.fecha.isBefore(hoy) -> R.string.tiempo_fecha_pasada
        else -> R.string.tiempo_fecha_futura
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (esHoy) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = nombreDia, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(
                        etiquetaFecha,
                        dia.fecha.format(formatoFechaTiempo)
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconoCielo(cielo)

            Text(
                text = stringResource(cielo.descripcion),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun IconoCielo(cielo: PresentacionCielo) {
    // Los iconos son decorativos: el texto contiguo ya explica el estado del cielo.
    if (cielo.solConNubes) {
        Box(modifier = Modifier.size(48.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_tiempo_soleado),
                contentDescription = null,
                modifier = Modifier.size(30.dp).align(Alignment.TopEnd)
            )
            Icon(
                painter = painterResource(cielo.icono),
                contentDescription = null,
                modifier = Modifier.size(38.dp).align(Alignment.BottomStart)
            )
        }
    } else {
        Icon(
            painter = painterResource(cielo.icono),
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
    }
}

