package com.example.theweatheroracle.model

interface WeatherRepository {
    suspend fun fetchWeatherForecast(
        latitude: Double,
        longitude: Double,
        units: String? = "standard",
        mode: String? = "json",
        cnt: Int? = null,
        lang: String? = null
    ): Result<WeatherForecastResponse>

    suspend fun fetchForecastByCityId(
        cityId: Int,
        units: String? = "standard",
        mode: String? = "json",
        cnt: Int? = null,
        lang: String? = null
    ): Result<WeatherForecastResponse>

    suspend fun fetchWeatherByLatLon(
        latitude: Double,
        longitude: Double,
        units: String? = "standard",
        mode: String? = "json",
        lang: String? = null
    ): Result<WeatherResponse>

    suspend fun fetchWeatherByCityId(
        cityId: Int,
        units: String? = "standard",
        mode: String? = "json",
        lang: String? = null
    ): Result<WeatherResponse>
}