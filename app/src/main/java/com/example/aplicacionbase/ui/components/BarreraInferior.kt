package com.example.aplicacionbase.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.example.aplicacionbase.ui.navigation.Destino

@Composable
fun BarraInferior(
    destinoActual: Destino,
    onSeleccionarDestino: (Destino) -> Unit
) {
    NavigationBar {
        Destino.entries.forEach { destino ->
            NavigationBarItem(
                selected = destinoActual == destino,
                onClick = {
                    onSeleccionarDestino(destino)
                },
                icon = {
                    Icon(
                        painter = painterResource(
                            id = destino.icono
                        ),
                        contentDescription = null
                    )
                },
                label = {
                    Text(destino.titulo)
                }
            )
        }
    }
}