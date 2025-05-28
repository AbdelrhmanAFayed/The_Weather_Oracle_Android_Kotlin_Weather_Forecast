package com.example.theweatheroracle.repo

import com.example.theweatheroracle.model.api.WeatherRemoteDataSource
import com.example.theweatheroracle.model.weather.WeatherForecastResponse
import com.example.theweatheroracle.model.weather.WeatherResponse

class FakeWeatherRemoteDataSource(
    private val weatherResponses: MutableList<WeatherResponse>,
    private val forecastResponses: MutableList<WeatherForecastResponse>
) : WeatherRemoteDataSource {

    override suspend fun fetchWeatherByLatLon(
        latitude: Double,
        longitude: Double,
        units: String?,
        mode: String?,
        lang: String?
    ): Result<WeatherResponse> {
        val response = weatherResponses.find {
            it.coord.lat == latitude && it.coord.lon == longitude
        }
        return if (response != null) {
            Result.success(response)
        } else {
            Result.failure(Exception("Weather not found for lat=$latitude, lon=$longitude"))
        }
    }

    override suspend fun fetchWeatherByCityId(
        cityId: Int,
        units: String?,
        mode: String?,
        lang: String?
    ): Result<WeatherResponse> {
        val response = weatherResponses.find { it.id == cityId }
        return if (response != null) {
            Result.success(response)
        } else {
            Result.failure(Exception("Weather not found for cityId=$cityId"))
        }
    }

    override suspend fun fetchWeatherForecast(
        latitude: Double,
        longitude: Double,
        units: String?,
        mode: String?,
        cnt: Int?,
        lang: String?
    ): Result<WeatherForecastResponse> {
        val response = forecastResponses.find {
            it.city.coord.lat == latitude && it.city.coord.lon == longitude
        }
        return if (response != null) {
            Result.success(response)
        } else {
            Result.failure(Exception("Forecast not found for lat=$latitude, lon=$longitude"))
        }
    }

    override suspend fun fetchForecastByCityId(
        cityId: Int,
        units: String?,
        mode: String?,
        cnt: Int?,
        lang: String?
    ): Result<WeatherForecastResponse> {
        val response = forecastResponses.find { it.city.id == cityId }
        return if (response != null) {
            Result.success(response)
        } else {
            Result.failure(Exception("Forecast not found for cityId=$cityId"))
        }
    }
}