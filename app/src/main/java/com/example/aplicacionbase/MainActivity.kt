package com.example.aplicacionbase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.aplicacionbase.ui.Aplicacion
import com.example.aplicacionbase.ui.theme.AplicacionBaseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AplicacionBaseTheme {
                Aplicacion()
            }
        }
    }
}
