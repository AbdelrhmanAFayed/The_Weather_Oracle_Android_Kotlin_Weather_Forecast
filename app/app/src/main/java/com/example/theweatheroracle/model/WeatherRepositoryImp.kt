package com.example.theweatheroracle.model

import com.example.theweatheroracle.model.db.WeatherLocalDataSource
import com.example.theweatheroracle.model.network.WeatherRemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class WeatherRepositoryImp private constructor(
    private val remoteDataSource: WeatherRemoteDataSource,
    private val localDataSource: WeatherLocalDataSource
) : WeatherRepository {
    companion object {
        @Volatile
        private var instance: WeatherRepositoryImp? = null

        fun getInstance(
            remoteDataSource: WeatherRemoteDataSource,
            localDataSource: WeatherLocalDataSource
        ): WeatherRepositoryImp {
            return instance ?: synchronized(this) {
                instance ?: WeatherRepositoryImp(remoteDataSource, localDataSource).also { instance = it }
            }
        }
    }

    override suspend fun fetchWeatherByLatLon(
        latitude: Double,
        longitude: Double,
        units: String?,
        mode: String?,
        lang: String?
    ): Result<WeatherResponse> {
        return remoteDataSource.fetchWeatherByLatLon(latitude, longitude, units, mode, lang)
    }

    override suspend fun fetchWeatherByCityId(
        cityId: Int,
        units: String?,
        mode: String?,
        lang: String?
    ): Result<WeatherResponse> {
        return remoteDataSource.fetchWeatherByCityId(cityId, units, mode, lang)
    }

    override suspend fun fetchWeatherForecast(
        latitude: Double,
        longitude: Double,
        units: String?,
        mode: String?,
        cnt: Int?,
        lang: String?
    ): Result<WeatherForecastResponse> {
        return remoteDataSource.fetchWeatherForecast(latitude, longitude, units, mode, cnt, lang)
    }

    override suspend fun fetchForecastByCityId(
        cityId: Int,
        units: String?,
        mode: String?,
        cnt: Int?,
        lang: String?
    ): Result<WeatherForecastResponse> {
        return remoteDataSource.fetchForecastByCityId(cityId, units, mode, cnt, lang)
    }
}