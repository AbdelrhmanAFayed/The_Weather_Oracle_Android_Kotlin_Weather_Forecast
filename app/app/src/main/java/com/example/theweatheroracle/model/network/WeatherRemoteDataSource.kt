package com.example.theweatheroracle.model.network

import com.example.theweatheroracle.model.WeatherForecastResponse
import com.example.theweatheroracle.model.WeatherResponse

interface WeatherRemoteDataSource {
    suspend fun getWeatherForecast(
        latitude: Double,
        longitude: Double,
        units: String? = "standard",
        mode: String? = "json",
        cnt: Int? = null,
        lang: String? = null
    ): WeatherForecastResponse

    suspend fun getForecastByCityId(
        cityId: Int,
        units: String? = "standard",
        mode: String? = "json",
        cnt: Int? = null,
        lang: String? = null
    ): WeatherForecastResponse

    suspend fun getWeatherByLatLon(
        latitude: Double,
        longitude: Double,
        units: String? = "standard",
        mode: String? = "json",
        lang: String? = null
    ): WeatherResponse

    suspend fun getWeatherByCityId(
        cityId: Int,
        units: String? = "standard",
        mode: String? = "json",
        lang: String? = null
    ): WeatherResponse
}