package com.example.aplicacionbase.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.aplicacionbase.ui.components.ElementoSuperpuesto

@Composable
fun PantallaBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        ElementoSuperpuesto(
            texto = "Caja 1",
            modifier = Modifier.align(
                Alignment.TopStart
            )
        )

        ElementoSuperpuesto(
            texto = "Caja 2",
            modifier = Modifier.align(
                Alignment.Center
            )
        )

        ElementoSuperpuesto(
            texto = "Caja 3",
            modifier = Modifier.align(
                Alignment.BottomEnd
            )
        )
    }
}