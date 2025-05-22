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
        ): WeatherRepositoryImp =
            instance ?: synchronized(this) {
                instance ?: WeatherRepositoryImp(remoteDataSource, localDataSource).also { instance = it }
            }
    }

    override suspend fun fetchWeatherForecast(
        latitude: Double,
        longitude: Double,
        units: String?,
        mode: String?,
        cnt: Int?,
        lang: String?
    ): WeatherForecastResponse? = withContext(Dispatchers.IO) {
        try {
            remoteDataSource.getWeatherForecast(latitude, longitude, units, mode, cnt, lang)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: HttpException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun fetchForecastByCityId(
        cityId: Int,
        units: String?,
        mode: String?,
        cnt: Int?,
        lang: String?
    ): WeatherForecastResponse? = withContext(Dispatchers.IO) {
        try {
            remoteDataSource.getForecastByCityId(cityId, units, mode, cnt, lang)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: HttpException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun fetchWeatherByLatLon(
        latitude: Double,
        longitude: Double,
        units: String?,
        mode: String?,
        lang: String?
    ): WeatherResponse? = withContext(Dispatchers.IO) {
        try {
            remoteDataSource.getWeatherByLatLon(latitude, longitude, units, mode, lang)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: HttpException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun fetchWeatherByCityId(
        cityId: Int,
        units: String?,
        mode: String?,
        lang: String?
    ): WeatherResponse? = withContext(Dispatchers.IO) {
        try {
            remoteDataSource.getWeatherByCityId(cityId, units, mode, lang)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: HttpException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}