package com.example.aplicacionbase.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ClienteTiempo {
    // Una sola instancia, compartida durante la vida del proceso.
    val api: TiempoApi by lazy {
        val clienteHttp = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/") // La barra final es necesaria.
            .client(clienteHttp)
            // Aquí se configura la DESERIALIZACIÓN automática JSON -> DTO.
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TiempoApi::class.java)
    }
}

