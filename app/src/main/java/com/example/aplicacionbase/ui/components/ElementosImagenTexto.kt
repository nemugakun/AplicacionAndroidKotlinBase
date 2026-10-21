package com.example.aplicacionbase.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aplicacionbase.R

// Imagen reutilizada por los tres componentes.
@Composable
private fun ImagenAplicacion(modifier: Modifier) {
    Box(
        modifier = modifier.clip(
            RoundedCornerShape(12.dp)
        )
    ) {
        Image(
            painter = painterResource(
                R.drawable.ic_launcher_background
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )

        Image(
            painter = painterResource(
                R.drawable.ic_launcher_foreground
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}

// Imagen a la izquierda y texto a la derecha.
@Composable
fun ElementoHorizontal(
    texto: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ImagenAplicacion(
            modifier = Modifier.size(56.dp)
        )

        Text(
            text = texto,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// Imagen arriba y texto debajo.
@Composable
fun ElementoVertical(
    texto: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ImagenAplicacion(
            modifier = Modifier.size(72.dp)
        )

        Text(
            text = texto,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// Texto superpuesto sobre la parte inferior de la imagen.
@Composable
fun ElementoSuperpuesto(
    texto: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(128.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        ImagenAplicacion(
            modifier = Modifier.fillMaxSize()
        )

        Text(
            text = texto,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surface.copy(
                        alpha = 0.85f
                    )
                )
                .padding(8.dp)
        )
    }
}