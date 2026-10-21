package com.example.aplicacionbase.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.aplicacionbase.ui.screens.PantallaBox
import com.example.aplicacionbase.ui.screens.PantallaElementos
import com.example.aplicacionbase.ui.screens.PantallaEventos
import com.example.aplicacionbase.ui.screens.PantallaRow
import com.example.aplicacionbase.ui.screens.PantallaColumn

@Composable
fun Navegacion(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Destino.COLUMN.ruta,
        modifier = modifier
    ) {
        composable(Destino.COLUMN.ruta) {
            PantallaColumn()
        }
        composable(Destino.ROW.ruta) {
            PantallaRow()
        }
        composable(Destino.BOX.ruta) {
            PantallaBox()
        }
        composable(Destino.ELEMENTOS.ruta) {
            PantallaElementos()
        }
        composable(Destino.EVENTOS.ruta) {
            PantallaEventos()
        }
    }
}