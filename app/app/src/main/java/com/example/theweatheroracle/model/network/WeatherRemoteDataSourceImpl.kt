package com.example.theweatheroracle.model.network

import com.example.theweatheroracle.model.WeatherForecastResponse
import com.example.theweatheroracle.model.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WeatherRemoteDataSourceImpl : WeatherRemoteDataSource {
    private val weatherService: WeatherService by lazy {
        RetrofitClient.weatherService
    }

    override suspend fun fetchWeatherByLatLon(
        latitude: Double,
        longitude: Double,
        units: String?,
        mode: String?,
        lang: String?
    ): Result<WeatherResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val call = weatherService.getWeatherByLatLon(latitude, longitude, units = units, mode = mode, lang = lang)
                val response = call.execute()
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Throwable("Empty response body"))
                } else {
                    Result.failure(Throwable("HTTP error: ${response.code()} - ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun fetchWeatherByCityId(
        cityId: Int,
        units: String?,
        mode: String?,
        lang: String?
    ): Result<WeatherResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val call = weatherService.getCurrentWeatherByCityId(cityId, units = units, mode = mode, lang = lang)
                val response = call.execute()
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Throwable("Empty response body"))
                } else {
                    Result.failure(Throwable("HTTP error: ${response.code()} - ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
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
        return withContext(Dispatchers.IO) {
            try {
                val call = weatherService.getWeatherForecast(latitude, longitude, units = units, mode = mode, cnt = cnt, lang = lang)
                val response = call.execute()
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Throwable("Empty response body"))
                } else {
                    Result.failure(Throwable("HTTP error: ${response.code()} - ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun fetchForecastByCityId(
        cityId: Int,
        units: String?,
        mode: String?,
        cnt: Int?,
        lang: String?
    ): Result<WeatherForecastResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val call = weatherService.getWeatherForecastByCityId(cityId, units = units, mode = mode, cnt = cnt, lang = lang)
                val response = call.execute()
                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) }
                        ?: Result.failure(Throwable("Empty response body"))
                } else {
                    Result.failure(Throwable("HTTP error: ${response.code()} - ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}