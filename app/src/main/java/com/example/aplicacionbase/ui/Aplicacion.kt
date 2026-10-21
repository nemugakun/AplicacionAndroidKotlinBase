package com.example.aplicacionbase.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.aplicacionbase.R
import com.example.aplicacionbase.ui.components.BarraInferior
import com.example.aplicacionbase.ui.navigation.Destino
import com.example.aplicacionbase.ui.navigation.Navegacion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Aplicacion() {
    val navController = rememberNavController()

    val entradaActual by
    navController.currentBackStackEntryAsState()

    val rutaActual = entradaActual?.destination?.route

    val destinoActual = Destino.entries.firstOrNull {
        it.ruta == rutaActual
    } ?: Destino.COLUMN

    Scaffold(
        modifier = Modifier.fillMaxSize(),

        topBar = {
            TopAppBar(
                title = {
                    Text(destinoActual.titulo)
                },
                navigationIcon = {
                    Image(
                        painter = painterResource(
                            id = R.drawable.ic_launcher_foreground
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(40.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor =
                        MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor =
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },

        bottomBar = {
            BarraInferior(
                destinoActual = destinoActual,
                onSeleccionarDestino = { destino ->
                    if (destino != destinoActual) {
                        navController.navigate(destino.ruta) {
                            popUpTo(
                                navController.graph
                                    .findStartDestination().id
                            ) {
                                saveState = true
                            }

                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { paddingInterior ->
        Navegacion(
            navController = navController,
            modifier = Modifier
                .padding(paddingInterior)
                .padding(16.dp)
                .fillMaxSize()
        )
    }
}