package com.example.aplicacionbase.ui.screens

/*
 * Catálogo de controles para la cuarta pestaña: Elementos.
 *
 * Integración en el proyecto del ejemplo:
 * 1. Copiar este archivo al paquete ui.screens.
 * 2. Añadir ELEMENTOS("elementos", "Elementos", R.drawable.ic_elementos)
 *    al enum Destino, tras crear ic_elementos con Vector Asset.
 * 3. Añadir al NavHost:
 *    composable(Destino.ELEMENTOS.ruta) { PantallaElementos() }
 *
 * Reutiliza únicamente los recursos ya creados en el proyecto:
 * ic_launcher_background, ic_launcher_foreground e ic_box.
 * No necesita una biblioteca adicional de iconos ni imágenes.
 */

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.aplicacionbase.R
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/*
 * No guardamos objetos Text, Image o Button.
 * Guardamos funciones que describen qué mostrar en cada sección.
 */
private data class EjemploElemento(
    val nombre: String,
    val contenido: @Composable () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaElementos(modifier: Modifier = Modifier) {
    /*
     * El estado se mantiene en la pantalla, fuera de los elementos de la lista.
     * Así, desplazar un control fuera de la zona visible no borra su valor.
     */
    var pulsaciones by rememberSaveable { mutableIntStateOf(0) }
    var textoRelleno by rememberSaveable { mutableStateOf("") }
    var textoBorde by rememberSaveable { mutableStateOf("") }
    var textoMultilinea by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var marcado by rememberSaveable { mutableStateOf(false) }
    var estadoTriple by rememberSaveable { mutableIntStateOf(0) }
    var turno by rememberSaveable { mutableStateOf("Mañana") }
    var notificaciones by rememberSaveable { mutableStateOf(false) }
    var curso by rememberSaveable { mutableStateOf("DAM") }
    var menuAbierto by remember { mutableStateOf(false) }
    var progreso by rememberSaveable { mutableFloatStateOf(0.4f) }
    var inicioRango by rememberSaveable { mutableFloatStateOf(20f) }
    var finRango by rememberSaveable { mutableFloatStateOf(80f) }
    var filtroActivo by rememberSaveable { mutableStateOf(false) }
    var mostrarFecha by rememberSaveable { mutableStateOf(false) }
    var fechaConfirmada by rememberSaveable { mutableStateOf<Long?>(null) }
    var mostrarReloj by rememberSaveable { mutableStateOf(false) }
    var mostrarDialogo by rememberSaveable { mutableStateOf(false) }
    var resultadoDialogo by rememberSaveable {
        mutableStateOf("Todavía no has respondido al diálogo.")
    }

    val estadoFecha = rememberDatePickerState()
    val estadoHora = rememberTimePickerState(
        initialHour = 12,
        initialMinute = 0,
        is24Hour = true
    )
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val colorResaltado = MaterialTheme.colorScheme.primary

    /*
     * Esta lista contiene lambdas @Composable heterogéneas.
     * Se reconstruye para describir la interfaz con los valores actuales.
     */
    val ejemplos = listOf(
        EjemploElemento("Text: etiqueta") {
            Text("Una etiqueta de texto, equivalente a un Label.")
        },

        EjemploElemento("Image: imagen") {
            LogoAplicacion()
            Text("El icono de la aplicación creado por la plantilla.")
        },

        EjemploElemento("Icon: icono") {
            IconoEjemplo()
            Text("Icon utiliza los colores del tema para dibujar el icono.")
        },

        EjemploElemento("Button y sus variantes") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { pulsaciones++ }) {
                    Text("Button")
                }
                OutlinedButton(onClick = { pulsaciones++ }) {
                    Text("OutlinedButton")
                }
                FilledTonalButton(onClick = { pulsaciones++ }) {
                    Text("FilledTonalButton")
                }
                TextButton(onClick = { pulsaciones++ }) {
                    Text("TextButton")
                }
                Text("Pulsaciones: $pulsaciones")
            }
        },

        EjemploElemento("IconButton y FloatingActionButton") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(onClick = { pulsaciones++ }) {
                    IconoEjemplo("Sumar una pulsación")
                }
                FloatingActionButton(onClick = { pulsaciones++ }) {
                    IconoEjemplo("Sumar una pulsación")
                }
            }
            Text("Pulsaciones: $pulsaciones")
        },

        EjemploElemento("TextField y OutlinedTextField") {
            TextField(
                value = textoRelleno,
                onValueChange = { textoRelleno = it },
                label = { Text("Campo con fondo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = textoBorde,
                onValueChange = { textoBorde = it },
                label = { Text("Campo con borde") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },

        EjemploElemento("Campo de texto multilínea") {
            OutlinedTextField(
                value = textoMultilinea,
                onValueChange = { textoMultilinea = it },
                label = { Text("Observaciones") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )
        },

        EjemploElemento("Campo de contraseña") {
            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                label = { Text("Contraseña de ejemplo") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },

        EjemploElemento("Checkbox y TriStateCheckbox") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .toggleable(
                        value = marcado,
                        role = Role.Checkbox,
                        onValueChange = { marcado = it }
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(
                    checked = marcado,
                    onCheckedChange = null
                )
                Text("Casilla marcada: $marcado")
            }

            val seleccionTriple = when (estadoTriple) {
                0 -> ToggleableState.Off
                1 -> ToggleableState.On
                else -> ToggleableState.Indeterminate
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .triStateToggleable(
                        state = seleccionTriple,
                        role = Role.Checkbox,
                        onClick = { estadoTriple = (estadoTriple + 1) % 3 }
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TriStateCheckbox(
                    state = seleccionTriple,
                    onClick = null
                )
                Text(
                    when (seleccionTriple) {
                        ToggleableState.Off -> "Desmarcado"
                        ToggleableState.On -> "Marcado"
                        ToggleableState.Indeterminate -> "Indeterminado"
                    }
                )
            }
        },

        EjemploElemento("RadioButton: selección única") {
            Column(modifier = Modifier.selectableGroup()) {
                listOf("Mañana", "Tarde").forEach { opcion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .selectable(
                                selected = turno == opcion,
                                role = Role.RadioButton,
                                onClick = { turno = opcion }
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RadioButton(
                            selected = turno == opcion,
                            onClick = null
                        )
                        Text(opcion)
                    }
                }
            }
            Text("Turno seleccionado: $turno")
        },

        EjemploElemento("Switch: interruptor") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .toggleable(
                        value = notificaciones,
                        role = Role.Switch,
                        onValueChange = { notificaciones = it }
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Switch(
                    checked = notificaciones,
                    onCheckedChange = null
                )
                Text("Activar notificaciones")
            }
            Text("Activado: $notificaciones")
        },

        EjemploElemento("Desplegable: ExposedDropdownMenuBox") {
            ExposedDropdownMenuBox(
                expanded = menuAbierto,
                onExpandedChange = { menuAbierto = it }
            ) {
                OutlinedTextField(
                    value = curso,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ciclo") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = menuAbierto
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            ExposedDropdownMenuAnchorType.PrimaryNotEditable
                        )
                )
                ExposedDropdownMenu(
                    expanded = menuAbierto,
                    onDismissRequest = { menuAbierto = false }
                ) {
                    listOf("DAM", "DAW", "SMR").forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion) },
                            onClick = {
                                curso = opcion
                                menuAbierto = false
                            }
                        )
                    }
                }
            }
        },

        EjemploElemento("Slider y RangeSlider") {
            Text("Progreso: " + (progreso * 100).toInt() + "%")
            Slider(
                value = progreso,
                onValueChange = { progreso = it },
                valueRange = 0f..1f
            )

            Text(
                "Intervalo: " + inicioRango.toInt() +
                    " a " + finRango.toInt()
            )
            RangeSlider(
                value = inicioRango..finRango,
                onValueChange = {
                    inicioRango = it.start
                    finRango = it.endInclusive
                },
                valueRange = 0f..100f
            )
        },

        EjemploElemento("DatePicker: selector de fecha") {
            Text("Fecha: " + formatearFecha(fechaConfirmada))
            OutlinedButton(
                onClick = {
                    estadoFecha.selectedDateMillis = fechaConfirmada
                    mostrarFecha = true
                }
            ) {
                Text("Elegir fecha")
            }
        },

        EjemploElemento("TimeInput y TimePicker: hora") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Switch(
                    checked = mostrarReloj,
                    onCheckedChange = { mostrarReloj = it }
                )
                Text("Usar reloj")
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (mostrarReloj) {
                    TimePicker(state = estadoHora)
                } else {
                    TimeInput(state = estadoHora)
                }
            }

            Text(
                "Hora: " + String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    estadoHora.hour,
                    estadoHora.minute
                )
            )
        },

        EjemploElemento("Indicadores de progreso") {
            Text("Comparten el valor del Slider anterior.")
            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier.fillMaxWidth()
            )
            CircularProgressIndicator(
                progress = { progreso },
                modifier = Modifier.size(48.dp)
            )
            Text("Indicador sin porcentaje conocido:")
            CircularProgressIndicator(modifier = Modifier.size(32.dp))
        },

        EjemploElemento("AssistChip y FilterChip") {
            AssistChip(
                onClick = { pulsaciones++ },
                label = { Text("Acción") }
            )
            FilterChip(
                selected = filtroActivo,
                onClick = { filtroActivo = !filtroActivo },
                label = { Text("Filtro activo") }
            )
        },

        EjemploElemento("Badge: contador sobre un icono") {
            BadgedBox(
                badge = {
                    Badge {
                        Text(pulsaciones.toString())
                    }
                }
            ) {
                IconoEjemplo("Pulsaciones: $pulsaciones")
            }
        },

        EjemploElemento("ListItem: elemento de una lista") {
            ListItem(
                headlineContent = { Text("Título del elemento") },
                supportingContent = { Text("Texto complementario") },
                leadingContent = {
                    LogoAplicacion(Modifier.size(40.dp))
                }
            )
        },

        EjemploElemento("Card, Surface y HorizontalDivider") {
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Contenido dentro de una tarjeta con borde.",
                    modifier = Modifier.padding(12.dp)
                )
            }
            HorizontalDivider()
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Surface proporciona fondo, forma y color.",
                    modifier = Modifier.padding(12.dp)
                )
            }
        },

        EjemploElemento("Snackbar: aviso breve") {
            Button(
                onClick = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Este es un mensaje de ejemplo.",
                            actionLabel = "Entendido",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            ) {
                Text("Mostrar aviso")
            }
            Text("El aviso aparecerá en la parte inferior de esta pantalla.")
        },

        EjemploElemento("AlertDialog: diálogo") {
            Button(onClick = { mostrarDialogo = true }) {
                Text("Abrir diálogo")
            }
            Text(resultadoDialogo)
        },

        EjemploElemento("Text con formato: AnnotatedString") {
            val textoConFormato = buildAnnotatedString {
                append("Un mismo texto puede incluir ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("negrita")
                }
                append(" y ")
                withStyle(SpanStyle(color = colorResaltado)) {
                    append("otro color")
                }
                append(".")
            }
            Text(textoConFormato)
            Text("Este ejemplo muestra texto con formato; no es un editor.")
        }
    )

    /*
     * La lista ocupa el contenido de la pestaña.
     * SnackbarHost se superpone en su borde inferior mediante Box.
     * Las claves estables identifican cada sección del catálogo.
     */
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(
                items = ejemplos,
                key = { it.nombre }
            ) { ejemplo ->
                TarjetaEjemplo(
                    nombre = ejemplo.nombre,
                    contenido = ejemplo.contenido
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )
    }

    /*
     * Los diálogos se declaran fuera de los items de LazyColumn.
     * Su presencia depende del estado de la pantalla, no de que la tarjeta
     * que los abrió siga dentro de la zona visible de la lista.
     */
    if (mostrarFecha) {
        DatePickerDialog(
            onDismissRequest = { mostrarFecha = false },
            confirmButton = {
                TextButton(
                    enabled = estadoFecha.selectedDateMillis != null,
                    onClick = {
                        fechaConfirmada = estadoFecha.selectedDateMillis
                        mostrarFecha = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarFecha = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = estadoFecha)
        }
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Diálogo de ejemplo") },
            text = { Text("¿Quieres confirmar esta acción de demostración?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        resultadoDialogo = "Has pulsado Aceptar."
                        mostrarDialogo = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        resultadoDialogo = "Has pulsado Cancelar."
                        mostrarDialogo = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TarjetaEjemplo(
    nombre: String,
    contenido: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = nombre,
                style = MaterialTheme.typography.titleMedium
            )
            HorizontalDivider()
            contenido()
        }
    }
}

@Composable
private fun IconoEjemplo(descripcion: String? = null) {
    Icon(
        painter = painterResource(R.drawable.ic_box),
        contentDescription = descripcion
    )
}

@Composable
private fun LogoAplicacion(
    modifier: Modifier = Modifier.size(72.dp)
) {
    Box(modifier = modifier.clip(RoundedCornerShape(12.dp))) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun formatearFecha(milisegundos: Long?): String {
    if (milisegundos == null) return "sin seleccionar"

    // DatePicker representa los días mediante medianoche UTC.
    // SimpleDateFormat permite usarlo también con el SDK mínimo de la plantilla.
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(milisegundos))
}

