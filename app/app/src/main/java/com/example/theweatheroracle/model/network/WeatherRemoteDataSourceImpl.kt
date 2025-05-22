package com.example.theweatheroracle.model.network

import com.example.theweatheroracle.model.WeatherForecastResponse
import com.example.theweatheroracle.model.WeatherResponse

object WeatherRemoteDataSourceImpl : WeatherRemoteDataSource {
    private val weatherService = RetrofitClient.weatherService

    override suspend fun getWeatherForecast(
        latitude: Double,
        longitude: Double,
        units: String?,
        mode: String?,
        cnt: Int?,
        lang: String?
    ): WeatherForecastResponse {
        return weatherService.getWeatherForecast(
            latitude = latitude,
            longitude = longitude,
            units = units,
            mode = mode,
            cnt = cnt,
            lang = lang
        )
    }

    override suspend fun getForecastByCityId(
        cityId: Int,
        units: String?,
        mode: String?,
        cnt: Int?,
        lang: String?
    ): WeatherForecastResponse {
        return weatherService.getWeatherForecastByCityId(
            cityId = cityId,
            units = units,
            mode = mode,
            cnt = cnt,
            lang = lang
        )
    }

    override suspend fun getWeatherByLatLon(
        latitude: Double,
        longitude: Double,
        units: String?,
        mode: String?,
        lang: String?
    ): WeatherResponse {
        return weatherService.getWeatherByLatLon(
            latitude = latitude,
            longitude = longitude,
            units = units,
            mode = mode,
            lang = lang
        )
    }

    override suspend fun getWeatherByCityId(
        cityId: Int,
        units: String?,
        mode: String?,
        lang: String?
    ): WeatherResponse {
        return weatherService.getCurrentWeatherByCityId(
            cityId = cityId,
            units = units,
            mode = mode,
            lang = lang
        )
    }
}