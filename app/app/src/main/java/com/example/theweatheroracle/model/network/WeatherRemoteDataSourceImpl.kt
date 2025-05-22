package com.example.theweatheroracle.model.network

import com.example.theweatheroracle.model.WeatherForecastResponse
import com.example.theweatheroracle.model.WeatherResponse

object WeatherRemoteDataSourceImpl : WeatherRemoteDataSource {
    private val weatherService = RetrofitClient.weatherService

    override suspend fun getWeatherForecast(latitude: Double, longitude: Double): WeatherForecastResponse {
        return weatherService.getWeatherForecast(latitude, longitude)
    }

    override suspend fun getForecastByCityId(cityId: Int): WeatherForecastResponse {
        return weatherService.getForecastByCityId(cityId)
    }

    override suspend fun getWeatherByLatLon(latitude: Double, longitude: Double): WeatherResponse {
        return weatherService.getWeatherByLatLon(latitude, longitude)
    }

    override suspend fun getWeatherByCityId(cityId: Int): WeatherResponse {
        return weatherService.getWeatherByCityId(cityId)
    }
}