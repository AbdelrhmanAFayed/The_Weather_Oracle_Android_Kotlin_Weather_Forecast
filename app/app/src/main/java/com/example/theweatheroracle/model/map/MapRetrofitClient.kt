package com.example.theweatheroracle.model.map

import com.example.theweatheroracle.model.api.WeatherService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MapRetrofitClient {
    private const val BASE_URL = "https://nominatim.openstreetmap.org/"

    val mapService: NominatimService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NominatimService::class.java)
    }
}