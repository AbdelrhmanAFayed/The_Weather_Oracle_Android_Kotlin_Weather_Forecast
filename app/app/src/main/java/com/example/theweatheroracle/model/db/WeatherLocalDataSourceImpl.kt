package com.example.theweatheroracle.model.db

import android.content.Context
import com.example.theweatheroracle.model.City
import com.example.theweatheroracle.model.Forecast
import com.example.theweatheroracle.model.ForecastEntity
import com.example.theweatheroracle.model.Weather
import com.example.theweatheroracle.model.WeatherEntryEntity

class WeatherLocalDataSourceImpl private constructor(
    private val context: Context
) : WeatherLocalDataSource {
    private val appDatabase: AppDatabase by lazy { AppDatabase.getDatabase(context) }
    private val cityDao: CityDao by lazy { appDatabase.cityDao() }
    private val forecastEntityDao: ForecastEntityDao by lazy { appDatabase.forecastEntityDao() }
    private val weatherEntryEntityDao: WeatherEntryEntityDao by lazy { appDatabase.weatherEntryEntityDao() }

    companion object {
        @Volatile
        private var instance: WeatherLocalDataSourceImpl? = null

        fun getInstance(context: Context): WeatherLocalDataSourceImpl {
            return instance ?: synchronized(this) {
                instance ?: WeatherLocalDataSourceImpl(context).also { instance = it }
            }
        }
    }

    override suspend fun saveCity(city: City) {
        cityDao.insertCity(city)
    }

    override suspend fun getCityById(cityId: Int): City? {
        return cityDao.getCityById(cityId)
    }

    override suspend fun getAllCities(): List<City> {
        return cityDao.getAllCities()
    }

    override suspend fun deleteCityById(cityId: Int) {
        cityDao.deleteCityById(cityId)
    }

    override suspend fun deleteAllCities() {
        cityDao.deleteAllCities()
    }

    override suspend fun saveForecast(forecast: Forecast, cityId: Int) {
        val existingForecasts = forecastEntityDao.getForecastsForCityAndDt(cityId, forecast.dt)
        if (existingForecasts.isEmpty()) {
            val forecastEntity = ForecastEntity(forecast, cityId)
            val forecastId = forecastEntityDao.insertForecast(forecastEntity)
            val weatherEntries = forecast.weather.map { WeatherEntryEntity(it, forecastId) }
            weatherEntryEntityDao.insertWeatherEntries(weatherEntries)
        }
    }

    override suspend fun getForecastsForCity(cityId: Int): List<Forecast> {
        val forecastEntities = forecastEntityDao.getForecastsForCity(cityId)
        return forecastEntities.map { entity ->
            val weatherEntries = weatherEntryEntityDao.getWeatherEntriesForForecast(entity.forecastId)
            Forecast(entity, weatherEntries)
        }
    }

    override suspend fun getForecastsForCityAndDt(cityId: Int, dt: Long): List<Forecast> {
        val forecastEntities = forecastEntityDao.getForecastsForCityAndDt(cityId, dt)
        return forecastEntities.map { entity ->
            val weatherEntries = weatherEntryEntityDao.getWeatherEntriesForForecast(entity.forecastId)
            Forecast(entity, weatherEntries)
        }
    }

    override suspend fun getForecastsForCityAfterDt(cityId: Int, dt: Long): List<Forecast> {
        val forecastEntities = forecastEntityDao.getForecastsForCityAfterDt(cityId, dt)
        return forecastEntities.map { entity ->
            val weatherEntries = weatherEntryEntityDao.getWeatherEntriesForForecast(entity.forecastId)
            Forecast(entity, weatherEntries)
        }
    }

    override suspend fun getWeatherForForecast(forecast: Forecast): List<Weather> {
        val forecastEntities = forecastEntityDao.getForecastsForCityAndDt(
            cityId = 0, // Placeholder, needs actual cityId
            dt = forecast.dt
        )
        if (forecastEntities.isNotEmpty()) {
            val weatherEntries = weatherEntryEntityDao.getWeatherEntriesForForecast(forecastEntities[0].forecastId)
            return weatherEntries.map { Weather(it) }
        }
        return emptyList()
    }

    override suspend fun deleteForecastsForCity(cityId: Int) {
        forecastEntityDao.deleteForecastsForCity(cityId)
        // No need to delete weather entries explicitly due to CASCADE
    }

    override suspend fun deleteAllForecasts() {
        forecastEntityDao.deleteAllForecasts()
        // No need to delete weather entries explicitly due to CASCADE
    }
}