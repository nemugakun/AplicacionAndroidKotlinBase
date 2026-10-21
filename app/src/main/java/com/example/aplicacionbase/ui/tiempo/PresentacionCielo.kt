package com.example.aplicacionbase.ui.tiempo

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.aplicacionbase.R

internal data class PresentacionCielo(
    @StringRes val descripcion: Int,
    @DrawableRes val icono: Int,
    val solConNubes: Boolean = false
)

// La traducción es LOCAL: los códigos WMO tienen un significado establecido.
// No hace falta llamar a una API de traducción.
internal fun presentacionCielo(codigo: Int?): PresentacionCielo = when (codigo) {
    0 -> PresentacionCielo(R.string.tiempo_soleado, R.drawable.ic_tiempo_soleado)
    1 -> PresentacionCielo(R.string.tiempo_casi_despejado, R.drawable.ic_tiempo_soleado)
    2 -> PresentacionCielo(
        R.string.tiempo_parcialmente_nublado,
        R.drawable.ic_tiempo_nublado,
        solConNubes = true
    )
    3 -> PresentacionCielo(R.string.tiempo_nublado, R.drawable.ic_tiempo_nublado)
    45 -> PresentacionCielo(R.string.tiempo_niebla, R.drawable.ic_tiempo_niebla)
    48 -> PresentacionCielo(R.string.tiempo_niebla_escarcha, R.drawable.ic_tiempo_niebla)
    51 -> PresentacionCielo(R.string.tiempo_llovizna_ligera, R.drawable.ic_tiempo_lluvia)
    53 -> PresentacionCielo(R.string.tiempo_llovizna_moderada, R.drawable.ic_tiempo_lluvia)
    55 -> PresentacionCielo(R.string.tiempo_llovizna_intensa, R.drawable.ic_tiempo_lluvia)
    56 -> PresentacionCielo(R.string.tiempo_llovizna_helada_ligera, R.drawable.ic_tiempo_lluvia)
    57 -> PresentacionCielo(R.string.tiempo_llovizna_helada_intensa, R.drawable.ic_tiempo_lluvia)
    61 -> PresentacionCielo(R.string.tiempo_lluvia_ligera, R.drawable.ic_tiempo_lluvia)
    63 -> PresentacionCielo(R.string.tiempo_lluvia_moderada, R.drawable.ic_tiempo_lluvia)
    65 -> PresentacionCielo(R.string.tiempo_lluvia_intensa, R.drawable.ic_tiempo_lluvia)
    66 -> PresentacionCielo(R.string.tiempo_lluvia_helada_ligera, R.drawable.ic_tiempo_lluvia)
    67 -> PresentacionCielo(R.string.tiempo_lluvia_helada_intensa, R.drawable.ic_tiempo_lluvia)
    71 -> PresentacionCielo(R.string.tiempo_nevada_ligera, R.drawable.ic_tiempo_nieve)
    73 -> PresentacionCielo(R.string.tiempo_nevada_moderada, R.drawable.ic_tiempo_nieve)
    75 -> PresentacionCielo(R.string.tiempo_nevada_intensa, R.drawable.ic_tiempo_nieve)
    77 -> PresentacionCielo(R.string.tiempo_granos_nieve, R.drawable.ic_tiempo_nieve)
    80 -> PresentacionCielo(R.string.tiempo_chubascos_ligeros, R.drawable.ic_tiempo_lluvia)
    81 -> PresentacionCielo(R.string.tiempo_chubascos_moderados, R.drawable.ic_tiempo_lluvia)
    82 -> PresentacionCielo(R.string.tiempo_chubascos_intensos, R.drawable.ic_tiempo_lluvia)
    85 -> PresentacionCielo(R.string.tiempo_chubascos_nieve_ligeros, R.drawable.ic_tiempo_nieve)
    86 -> PresentacionCielo(R.string.tiempo_chubascos_nieve_intensos, R.drawable.ic_tiempo_nieve)
    95 -> PresentacionCielo(R.string.tiempo_tormenta, R.drawable.ic_tiempo_tormenta)
    96 -> PresentacionCielo(R.string.tiempo_tormenta_granizo_ligero, R.drawable.ic_tiempo_tormenta)
    97 -> PresentacionCielo(R.string.tiempo_tormenta_intensa, R.drawable.ic_tiempo_tormenta)
    99 -> PresentacionCielo(R.string.tiempo_tormenta_granizo_intenso, R.drawable.ic_tiempo_tormenta)
    null -> PresentacionCielo(R.string.tiempo_sin_datos, R.drawable.ic_tiempo_desconocido)
    else -> PresentacionCielo(R.string.tiempo_desconocido, R.drawable.ic_tiempo_desconocido)
}

