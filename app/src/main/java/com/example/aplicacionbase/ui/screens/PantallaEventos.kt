package com.example.aplicacionbase.ui.screens

/*
 * Quinta pestaña: Eventos.
 *
 * Integración:
 * 1. Guardar este archivo en ui.screens.
 * 2. Crear ic_eventos desde el icono Material touch_app con Vector Asset.
 * 3. Añadir EVENTOS("eventos", "Eventos", R.drawable.ic_eventos) a Destino.
 * 4. Añadir al NavHost:
 *    composable(Destino.EVENTOS.ruta) { PantallaEventos() }
 *
 * Reutiliza las bibliotecas de Compose y Navigation del proyecto anterior.
 * Para LocalLifecycleOwner, declara en dependencies del módulo app:
 * implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
 * El registro también se escribe en Logcat con la etiqueta EventosCompose.
 */

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private data class EjemploEvento(
    val nombre: String,
    val descripcion: String,
    val contenido: @Composable () -> Unit
)

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun PantallaEventos(modifier: Modifier = Modifier) {
    // Estado del registro. Se limita a las 20 entradas más recientes.
    var totalEventos by rememberSaveable { mutableIntStateOf(0) }
    var ultimoEvento by rememberSaveable {
        mutableStateOf("Interactúa con los controles o desplaza la lista.")
    }
    var historial by rememberSaveable { mutableStateOf("") }
    var mostrarRegistro by rememberSaveable { mutableStateOf(false) }

    // Estado de los controles, fuera de los items de LazyColumn.
    var pulsaciones by rememberSaveable { mutableIntStateOf(0) }
    var texto by rememberSaveable { mutableStateOf("") }
    var aceptado by rememberSaveable { mutableStateOf(false) }
    var notificaciones by rememberSaveable { mutableStateOf(false) }
    var turno by rememberSaveable { mutableStateOf("Mañana") }
    var valorSlider by rememberSaveable { mutableFloatStateOf(0.5f) }
    var valorConfirmado by rememberSaveable { mutableFloatStateOf(0.5f) }
    var nombre by rememberSaveable { mutableStateOf("") }
    var apellidos by rememberSaveable { mutableStateOf("") }
    var busqueda by rememberSaveable { mutableStateOf("") }
    var textoTecladoFisico by rememberSaveable { mutableStateOf("") }
    var ciclo by rememberSaveable { mutableStateOf("DAM") }
    var menuAbierto by remember { mutableStateOf(false) }
    var mostrarDialogo by rememberSaveable { mutableStateOf(false) }
    var interceptarAtras by rememberSaveable { mutableStateOf(false) }
    var bloqueGrande by rememberSaveable { mutableStateOf(false) }

    // El foco real se observa, no se restaura como si fuese un dato de formulario.
    var nombreTieneFoco by remember { mutableStateOf(false) }
    var apellidosTienenFoco by remember { mutableStateOf(false) }
    val focoNombre = remember { FocusRequester() }
    val focoApellidos = remember { FocusRequester() }
    val gestorFoco = LocalFocusManager.current
    val teclado = LocalSoftwareKeyboardController.current

    val estadoLista = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val fuentePulsacion = remember { MutableInteractionSource() }
    val estaPulsado by fuentePulsacion.collectIsPressedAsState()

    // Estas medidas y desplazamientos se expresan en píxeles físicos.
    var anchoArrastre by remember { mutableIntStateOf(0) }
    var desplazamientoX by remember { mutableFloatStateOf(0f) }
    var arrastrando by remember { mutableStateOf(false) }
    val anchoTiradorPx = with(LocalDensity.current) { 48.dp.toPx() }
    val limiteArrastre = (anchoArrastre - anchoTiradorPx).coerceAtLeast(0f)
    val estadoArrastre = rememberDraggableState { delta ->
        desplazamientoX = (desplazamientoX + delta)
            .coerceIn(0f, limiteArrastre)
    }

    var anchoMedido by remember { mutableIntStateOf(0) }
    var altoMedido by remember { mutableIntStateOf(0) }

    val propietarioCiclo = LocalLifecycleOwner.current
    var estadoCiclo by remember(propietarioCiclo) {
        mutableStateOf(propietarioCiclo.lifecycle.currentState.name)
    }

    /*
     * Los callbacks actualizan el estado. No se ejecutan acciones ni se
     * registra un evento simplemente por volver a componer un Text.
     */
    val registrar: (String) -> Unit = { mensaje ->
        totalEventos += 1
        ultimoEvento = mensaje
        val linea = "$totalEventos. $mensaje"
        historial = (
            listOf(linea) +
                historial.lineSequence().filter { it.isNotBlank() }.toList()
            )
            .take(20)
            .joinToString(separator = System.lineSeparator())
        Log.d("EventosCompose", linea)
    }

    // Los efectos de larga duración utilizan la versión actual del callback.
    val registrarActual by rememberUpdatedState(registrar)

    LaunchedEffect(fuentePulsacion) {
        fuentePulsacion.interactions.collect { interaccion ->
            when (interaccion) {
                is PressInteraction.Press ->
                    registrarActual("PressInteraction.Press: dedo o puntero presionado")
                is PressInteraction.Release ->
                    registrarActual("PressInteraction.Release: pulsación soltada")
                is PressInteraction.Cancel ->
                    registrarActual("PressInteraction.Cancel: pulsación cancelada")
                else -> Unit
            }
        }
    }

    LaunchedEffect(estadoLista) {
        // snapshotFlow observa cambios de estado: no existe un onScroll de View.
        launch {
            snapshotFlow { estadoLista.isScrollInProgress }
                .drop(1)
                .collect { desplazando ->
                    registrarActual(
                        if (desplazando) {
                            "LazyColumn: comienza el desplazamiento"
                        } else {
                            "LazyColumn: termina el desplazamiento"
                        }
                    )
                }
        }
        launch {
            snapshotFlow { estadoLista.firstVisibleItemIndex }
                .drop(1)
                .collect { indice ->
                    registrarActual(
                        "LazyColumn: primer bloque visible " + (indice + 1)
                    )
                }
        }
    }

    /*
     * NavHost proporciona un LifecycleOwner para cada destino.
     * Aquí observamos el ciclo de esta pantalla. El observador se retira
     * cuando su efecto deja la composición.
     */
    DisposableEffect(propietarioCiclo) {
        val observador = LifecycleEventObserver { _, evento ->
            estadoCiclo = propietarioCiclo.lifecycle.currentState.name
            registrarActual("Ciclo del destino: " + evento.name)
        }
        propietarioCiclo.lifecycle.addObserver(observador)

        onDispose {
            propietarioCiclo.lifecycle.removeObserver(observador)
            Log.d("EventosCompose", "Observador de ciclo retirado")
        }
    }

    // Se declara siempre, y se habilita mediante el estado.
    BackHandler(enabled = interceptarAtras) {
        interceptarAtras = false
        registrar(
            "BackHandler: Atrás interceptado. La siguiente pulsación navegará."
        )
    }

    val ejemplos = listOf(
        EjemploEvento(
            nombre = "1. onClick: pulsación",
            descripcion = "Pulsa el botón para ejecutar una acción."
        ) {
            Button(
                onClick = {
                    pulsaciones += 1
                    registrar("Button.onClick: pulsación $pulsaciones")
                }
            ) {
                Text("Pulsar")
            }
            Text("Pulsaciones: $pulsaciones")
        },

        EjemploEvento(
            nombre = "2. Pulsación simple, doble y larga",
            descripcion = "Prueba los tres gestos sobre la misma zona. " +
                "La pulsación simple espera brevemente para distinguir el doble toque."
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 88.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .combinedClickable(
                        role = Role.Button,
                        onClickLabel = "Pulsación simple",
                        onLongClickLabel = "Pulsación larga",
                        onClick = {
                            registrar("combinedClickable.onClick: toque simple")
                        },
                        onDoubleClick = {
                            registrar("combinedClickable.onDoubleClick: doble toque")
                        },
                        onLongClick = {
                            registrar("combinedClickable.onLongClick: pulsación larga")
                        }
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Toca, toca dos veces o mantén pulsado")
            }
        },

        EjemploEvento(
            nombre = "3. Presionar, soltar y cancelar",
            descripcion = "Mantén pulsado este botón y después suéltalo. " +
                "También puedes desplazar el dedo fuera para explorar la cancelación."
        ) {
            Button(
                onClick = { registrar("Button.onClick tras la pulsación") },
                interactionSource = fuentePulsacion
            ) {
                Text(if (estaPulsado) "Presionado" else "Mantener pulsado")
            }
            Text("Estado presionado: $estaPulsado")
            Text("El registro diferencia Press, Release y Cancel.")
        },

        EjemploEvento(
            nombre = "4. onValueChange: texto y validación",
            descripcion = "El callback se recibe al editar el campo. " +
                "La validación se calcula a partir de su contenido."
        ) {
            OutlinedTextField(
                value = texto,
                onValueChange = { nuevoTexto ->
                    texto = nuevoTexto
                    registrar(
                        "TextField.onValueChange: " +
                            nuevoTexto.length + " caracteres"
                    )
                },
                label = { Text("Escribe al menos tres caracteres") },
                isError = texto.isNotEmpty() && texto.length < 3,
                supportingText = {
                    Text(
                        if (texto.length >= 3) {
                            "Contenido válido"
                        } else {
                            "Mínimo: tres caracteres"
                        }
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                enabled = texto.length >= 3,
                onClick = {
                    registrar("Button.onClick: contenido validado")
                    gestorFoco.clearFocus()
                    teclado?.hide()
                }
            ) {
                Text("Validar")
            }
        },

        EjemploEvento(
            nombre = "5. onCheckedChange: casilla e interruptor",
            descripcion = "Los controles notifican el nuevo valor booleano."
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(
                    checked = aceptado,
                    onCheckedChange = { nuevoValor ->
                        aceptado = nuevoValor
                        registrar("Checkbox.onCheckedChange: $nuevoValor")
                    }
                )
                Text("Aceptar condiciones")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Switch(
                    checked = notificaciones,
                    onCheckedChange = { nuevoValor ->
                        notificaciones = nuevoValor
                        registrar("Switch.onCheckedChange: $nuevoValor")
                    }
                )
                Text("Notificaciones")
            }
        },

        EjemploEvento(
            nombre = "6. onClick: selección de una opción",
            descripcion = "Todas las opciones comparten una variable con el turno elegido. " +
                "La fila completa recibe la pulsación."
        ) {
            Column(modifier = Modifier.selectableGroup()) {
                listOf("Mañana", "Tarde").forEach { opcion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .selectable(
                                selected = turno == opcion,
                                role = Role.RadioButton,
                                onClick = {
                                    turno = opcion
                                    registrar("selectable.onClick: turno $opcion")
                                }
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
        },

        EjemploEvento(
            nombre = "7. onValueChangeFinished: slider",
            descripcion = "Compara los cambios continuos de valor con el aviso " +
                "que se recibe al terminar de ajustarlo."
        ) {
            Text("Valor actual: " + (valorSlider * 100).roundToInt())
            Slider(
                value = valorSlider,
                onValueChange = { nuevoValor ->
                    valorSlider = nuevoValor
                    registrar(
                        "Slider.onValueChange: " +
                            (nuevoValor * 100).roundToInt()
                    )
                },
                onValueChangeFinished = {
                    valorConfirmado = valorSlider
                    registrar(
                        "Slider.onValueChangeFinished: " +
                            (valorConfirmado * 100).roundToInt()
                    )
                }
            )
            Text(
                "Último valor confirmado: " +
                    (valorConfirmado * 100).roundToInt()
            )
        },

        EjemploEvento(
            nombre = "8. Foco y acciones Siguiente / Listo",
            descripcion = "Toca los campos para ver onFocusChanged. " +
                "Con el teclado en pantalla, Siguiente lleva al segundo campo " +
                "y Listo termina la edición."
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        registrar("KeyboardActions.onNext: pasar a Apellidos")
                        focoApellidos.requestFocus()
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focoNombre)
                    .onFocusChanged { estado ->
                        if (nombreTieneFoco != estado.isFocused) {
                            nombreTieneFoco = estado.isFocused
                            registrar("onFocusChanged: Nombre = " + estado.isFocused)
                        }
                    }
            )

            OutlinedTextField(
                value = apellidos,
                onValueChange = { apellidos = it },
                label = { Text("Apellidos") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        registrar("KeyboardActions.onDone: terminar edición")
                        gestorFoco.clearFocus()
                        teclado?.hide()
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focoApellidos)
                    .onFocusChanged { estado ->
                        if (apellidosTienenFoco != estado.isFocused) {
                            apellidosTienenFoco = estado.isFocused
                            registrar("onFocusChanged: Apellidos = " + estado.isFocused)
                        }
                    }
            )

            TextButton(
                onClick = {
                    registrar("FocusRequester.requestFocus: Nombre")
                    focoNombre.requestFocus()
                }
            ) {
                Text("Dar foco al nombre")
            }
        },

        EjemploEvento(
            nombre = "9. KeyboardActions.onSearch: buscar",
            descripcion = "La acción Buscar del teclado ejecuta el callback. " +
                "El botón ofrece la misma acción si utilizas un teclado físico."
        ) {
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                label = { Text("Búsqueda de prueba") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        registrar(
                            "KeyboardActions.onSearch: " +
                                busqueda.length + " caracteres"
                        )
                        gestorFoco.clearFocus()
                        teclado?.hide()
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    registrar(
                        "Button.onClick: búsqueda de " +
                            busqueda.length + " caracteres"
                    )
                    gestorFoco.clearFocus()
                    teclado?.hide()
                }
            ) {
                Text("Buscar")
            }
        },

        EjemploEvento(
            nombre = "10. onPreviewKeyEvent: teclado físico",
            descripcion = "Da foco al campo y escribe con el teclado físico. " +
                "Este ejemplo observa las teclas y devuelve false para permitir " +
                "que el campo siga procesándolas normalmente."
        ) {
            OutlinedTextField(
                value = textoTecladoFisico,
                onValueChange = { textoTecladoFisico = it },
                label = { Text("Prueba con el teclado del ordenador") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .onPreviewKeyEvent { evento ->
                        if (evento.type == KeyEventType.KeyDown) {
                            registrar("onPreviewKeyEvent: " + evento.key)
                        }
                        false
                    }
            )
            Text(
                "Los teclados en pantalla pueden introducir texto sin emitir " +
                    "eventos de tecla física."
            )
        },

        EjemploEvento(
            nombre = "11. Abrir, elegir y descartar un menú",
            descripcion = "Abrir el desplegable dispara onExpandedChange. " +
                "Elegir una opción ejecuta onClick; tocar fuera solicita " +
                "su cierre mediante onDismissRequest."
        ) {
            ExposedDropdownMenuBox(
                expanded = menuAbierto,
                onExpandedChange = { abierto ->
                    menuAbierto = abierto
                    registrar("Menú.onExpandedChange: $abierto")
                }
            ) {
                OutlinedTextField(
                    value = ciclo,
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
                    onDismissRequest = {
                        menuAbierto = false
                        registrar("Menú.onDismissRequest: descartado")
                    }
                ) {
                    listOf("DAM", "DAW", "SMR").forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion) },
                            onClick = {
                                ciclo = opcion
                                menuAbierto = false
                                registrar("DropdownMenuItem.onClick: $opcion")
                            }
                        )
                    }
                }
            }
        },

        EjemploEvento(
            nombre = "12. Diálogo: confirmar, cancelar y descartar",
            descripcion = "Compara los botones Aceptar y Cancelar con tocar " +
                "fuera del diálogo o utilizar Atrás."
        ) {
            Button(
                onClick = {
                    registrar("Button.onClick: abrir diálogo")
                    mostrarDialogo = true
                }
            ) {
                Text("Abrir diálogo")
            }
            Text(
                "Cambiar la variable para cerrar el diálogo no ejecuta " +
                    "automáticamente onDismissRequest."
            )
        },

        EjemploEvento(
            nombre = "13. Arrastre horizontal",
            descripcion = "Arrastra la zona para mover el tirador. " +
                "Se registran el comienzo y el final. El eje horizontal " +
                "permite mantener el desplazamiento vertical de la lista."
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .onSizeChanged { tamano ->
                        anchoArrastre = tamano.width
                        val limite = (tamano.width - anchoTiradorPx)
                            .coerceAtLeast(0f)
                        desplazamientoX = desplazamientoX.coerceIn(0f, limite)
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .draggable(
                        state = estadoArrastre,
                        orientation = Orientation.Horizontal,
                        onDragStarted = { _ ->
                            arrastrando = true
                            registrar("draggable.onDragStarted")
                        },
                        onDragStopped = { velocidad ->
                            arrastrando = false
                            registrar(
                                "draggable.onDragStopped: " +
                                    velocidad.roundToInt() + " px/s"
                            )
                        }
                    )
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset {
                            IntOffset(desplazamientoX.roundToInt(), 0)
                        }
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            Text(
                "Posición: " + desplazamientoX.roundToInt() +
                    " px. Arrastrando: $arrastrando"
            )
        },

        EjemploEvento(
            nombre = "14. Desplazamiento de LazyColumn",
            descripcion = "Observamos LazyListState mediante snapshotFlow. " +
                "El registro avisa del comienzo, final y cambio de primer bloque."
        ) {
            Text(
                "Primer bloque visible: " +
                    (estadoLista.firstVisibleItemIndex + 1)
            )
            Button(
                onClick = {
                    registrar("Button.onClick: desplazar al inicio")
                    gestorFoco.clearFocus()
                    teclado?.hide()
                    coroutineScope.launch {
                        estadoLista.animateScrollToItem(0)
                    }
                }
            ) {
                Text("Volver al inicio")
            }
        },

        EjemploEvento(
            nombre = "15. BackHandler: botón o gesto Atrás",
            descripcion = "Activa la captura y utiliza Atrás. " +
                "Se desactiva después de una captura para que la siguiente " +
                "pulsación permita navegar normalmente."
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Switch(
                    checked = interceptarAtras,
                    onCheckedChange = { activo ->
                        interceptarAtras = activo
                        if (activo) {
                            gestorFoco.clearFocus()
                            teclado?.hide()
                        }
                        registrar("Captura de Atrás habilitada: $activo")
                    }
                )
                Text("Capturar una pulsación de Atrás")
            }
            Text("Captura activa: $interceptarAtras")
        },

        EjemploEvento(
            nombre = "16. Ciclo de vida del destino",
            descripcion = "Dentro del NavHost observamos el ciclo de esta " +
                "pantalla con LifecycleEventObserver. Prueba a cambiar de " +
                "pestaña o enviar la app al fondo y regresar."
        ) {
            Text("Estado actual: $estadoCiclo")
            Text(
                "Ejemplos de señales: ON_START, ON_RESUME, ON_PAUSE y ON_STOP."
            )
            Text(
                "Los eventos también aparecen en Logcat con la etiqueta " +
                    "EventosCompose, incluso cuando esta pantalla no está visible."
            )
        },

        EjemploEvento(
            nombre = "17. onSizeChanged: cambios de tamaño",
            descripcion = "Este callback también se recibe con la primera " +
                "medida del componente. El botón cambia su altura para " +
                "provocar una nueva medida."
        ) {
            OutlinedButton(
                onClick = {
                    bloqueGrande = !bloqueGrande
                    registrar("Button.onClick: cambiar tamaño del bloque")
                }
            ) {
                Text(if (bloqueGrande) "Reducir" else "Ampliar")
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (bloqueGrande) 128.dp else 64.dp)
                    .onSizeChanged { tamano ->
                        anchoMedido = tamano.width
                        altoMedido = tamano.height
                        registrar(
                            "onSizeChanged: " +
                                tamano.width + " × " + tamano.height + " px"
                        )
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text("Área de medida")
            }
            Text(
                "Medida: $anchoMedido × $altoMedido píxeles. " +
                    "La altura del código se define en dp."
            )
        }
    )

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // El registro permanece accesible mientras se desplaza el catálogo.
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Eventos: $totalEventos",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Row {
                        TextButton(onClick = { mostrarRegistro = true }) {
                            Text("Registro")
                        }
                        TextButton(
                            onClick = {
                                totalEventos = 0
                                historial = ""
                                ultimoEvento = "Registro limpio."
                                Log.d("EventosCompose", "Registro limpiado")
                            }
                        ) {
                            Text("Limpiar")
                        }
                    }
                }
                Text(
                    text = ultimoEvento,
                    style = MaterialTheme.typography.bodySmall,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        LazyColumn(
            state = estadoLista,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(
                items = ejemplos,
                key = { it.nombre }
            ) { ejemplo ->
                TarjetaEvento(
                    nombre = ejemplo.nombre,
                    descripcion = ejemplo.descripcion,
                    contenido = ejemplo.contenido
                )
            }
        }
    }

    // Los diálogos quedan fuera de los items de LazyColumn.
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogo = false
                registrar("AlertDialog.onDismissRequest: fuera o Atrás")
            },
            title = { Text("Probar los eventos de un diálogo") },
            text = {
                Text(
                    "Acepta, cancela, toca fuera o utiliza Atrás. " +
                        "Después consulta el registro."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogo = false
                        registrar("AlertDialog: onClick de Aceptar")
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogo = false
                        registrar("AlertDialog: onClick de Cancelar")
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (mostrarRegistro) {
        AlertDialog(
            onDismissRequest = { mostrarRegistro = false },
            title = { Text("Últimos 20 eventos") },
            text = {
                Text(
                    text = historial.ifBlank { "Todavía no hay eventos." },
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState())
                )
            },
            confirmButton = {
                TextButton(onClick = { mostrarRegistro = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
private fun TarjetaEvento(
    nombre: String,
    descripcion: String,
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
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodySmall
            )
            HorizontalDivider()
            contenido()
        }
    }
}

