package com.example.theweatheroracle.model.db.weather

import com.example.theweatheroracle.model.weather.City
import com.example.theweatheroracle.model.weather.Forecast
import com.example.theweatheroracle.model.weather.Weather

interface WeatherLocalDataSource {
    suspend fun saveCity(city: City)
    suspend fun getCityById(cityId: Int): City?
    suspend fun getAllCities(): List<City>
    suspend fun deleteCityById(cityId: Int)
    suspend fun deleteAllCities()

    suspend fun saveForecast(forecast: Forecast, cityId: Int)
    suspend fun getForecastsForCity(cityId: Int): List<Forecast>
    suspend fun getForecastsForCityAndDt(cityId: Int, dt: Long): List<Forecast>
    suspend fun getForecastsForCityAfterDt(cityId: Int, dt: Long): List<Forecast>
    suspend fun getWeatherForForecast(forecast: Forecast): List<Weather>
    suspend fun deleteForecastsForCity(cityId: Int)
    suspend fun deleteAllForecasts()
    suspend fun deleteForecastsBeforeDt(id: Int, dt : Long)
}