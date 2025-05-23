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
        return withContext(Dispatchers.IO) {
            val result = remoteDataSource.fetchWeatherByLatLon(latitude, longitude, units, mode, lang)
            result.onSuccess { weather ->
                val city = City(
                    id = weather.id,
                    name = weather.name,
                    coord = Coord(weather.coord.lat, weather.coord.lon),
                    country = weather.sys.country ?: "Unknown",
                    population = 0,
                    timezone = weather.timezone,
                    sunrise = weather.sys.sunrise ?: 0L,
                    sunset = weather.sys.sunset ?: 0L
                )
                localDataSource.saveCity(city)
            }
            result
        }
    }

    override suspend fun fetchWeatherByCityId(
        cityId: Int,
        units: String?,
        mode: String?,
        lang: String?
    ): Result<WeatherResponse> {
        return withContext(Dispatchers.IO) {
            val result = remoteDataSource.fetchWeatherByCityId(cityId, units, mode, lang)
            result.onSuccess { weather ->
                val city = City(
                    id = weather.id,
                    name = weather.name,
                    coord = Coord(weather.coord.lat, weather.coord.lon),
                    country = weather.sys.country ?: "Unknown",
                    population = 0, // Not provided by API
                    timezone = weather.timezone,
                    sunrise = weather.sys.sunrise ?: 0L,
                    sunset = weather.sys.sunset ?: 0L
                )
                localDataSource.saveCity(city)
            }
            result
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
            val result = remoteDataSource.fetchWeatherForecast(latitude, longitude, units, mode, cnt, lang)
            result.onSuccess { forecastResponse ->
                localDataSource.saveCity(forecastResponse.city)

                forecastResponse.list.forEach { forecast ->
                    localDataSource.saveForecast(forecast, forecastResponse.city.id)
                }
            }
            result
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
            val result = remoteDataSource.fetchForecastByCityId(cityId, units, mode, cnt, lang)
            result.onSuccess { forecastResponse ->
                localDataSource.saveCity(forecastResponse.city)

                forecastResponse.list.forEach { forecast ->
                    localDataSource.saveForecast(forecast, forecastResponse.city.id)
                }
            }
            result
        }
    }

    override suspend fun getCityById(cityId: Int): City? {
        return withContext(Dispatchers.IO) {
            localDataSource.getCityById(cityId)
        }
    }

    override suspend fun getAllCities(): List<City> {
        return withContext(Dispatchers.IO) {
            localDataSource.getAllCities()
        }
    }

    override suspend fun deleteCityById(cityId: Int) {
        return withContext(Dispatchers.IO) {
            localDataSource.deleteCityById(cityId)
        }
    }

    override suspend fun deleteAllCities() {
        return withContext(Dispatchers.IO) {
            localDataSource.deleteAllCities()
        }
    }

    override suspend fun getForecastsForCity(cityId: Int): List<Forecast> {
        return withContext(Dispatchers.IO) {
            localDataSource.getForecastsForCity(cityId)
        }
    }

    override suspend fun getForecastsForCityAndDt(cityId: Int, dt: Long): List<Forecast> {
        return withContext(Dispatchers.IO) {
            localDataSource.getForecastsForCityAndDt(cityId, dt)
        }
    }

    override suspend fun getForecastsForCityAfterDt(cityId: Int, dt: Long): List<Forecast> {
        return withContext(Dispatchers.IO) {
            localDataSource.getForecastsForCityAfterDt(cityId, dt)
        }
    }

    override suspend fun deleteForecastsForCity(cityId: Int) {
        return withContext(Dispatchers.IO) {
            localDataSource.deleteForecastsForCity(cityId)
        }
    }

    override suspend fun deleteAllForecasts() {
        return withContext(Dispatchers.IO) {
            localDataSource.deleteAllForecasts()
        }
    }
}