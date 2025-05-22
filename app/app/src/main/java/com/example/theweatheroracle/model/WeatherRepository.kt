package com.example.theweatheroracle.model

interface WeatherRepository {
    suspend fun fetchWeatherForecast(latitude: Double, longitude: Double): WeatherForecastResponse?
    suspend fun fetchForecastByCityId(cityId: Int): WeatherForecastResponse?
    suspend fun fetchWeatherByLatLon(latitude: Double, longitude: Double): WeatherResponse?
    suspend fun fetchWeatherByCityId(cityId: Int): WeatherResponse?
}