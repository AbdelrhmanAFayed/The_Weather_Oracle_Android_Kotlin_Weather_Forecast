package com.example.theweatheroracle.model.network

import com.example.theweatheroracle.model.WeatherForecastResponse
import com.example.theweatheroracle.model.WeatherResponse

interface WeatherRemoteDataSource {
    suspend fun getWeatherForecast(latitude: Double, longitude: Double): WeatherForecastResponse
    suspend fun getForecastByCityId(cityId: Int): WeatherForecastResponse
    suspend fun getWeatherByLatLon(latitude: Double, longitude: Double): WeatherResponse
    suspend fun getWeatherByCityId(cityId: Int): WeatherResponse
}