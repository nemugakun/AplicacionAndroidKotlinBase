package com.example.aplicacionbase.ui.navigation

import com.example.aplicacionbase.R

enum class Destino(
    val ruta: String,
    val titulo: String,
    val icono: Int
) {
    COLUMN("column", "Column", R.drawable.ic_column),
    ROW("row", "Row", R.drawable.ic_row),
    BOX("box", "Box", R.drawable.ic_box),
    ELEMENTOS("elementos", "Elementos", R.drawable.ic_elementos),
    EVENTOS("eventos", "Eventos", R.drawable.ic_eventos)
}