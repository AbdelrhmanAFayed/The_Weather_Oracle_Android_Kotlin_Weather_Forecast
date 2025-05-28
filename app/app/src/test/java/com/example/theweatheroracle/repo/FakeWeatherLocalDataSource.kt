package com.example.theweatheroracle.repo


import com.example.theweatheroracle.model.db.weather.WeatherLocalDataSource
import com.example.theweatheroracle.model.weather.City
import com.example.theweatheroracle.model.weather.Forecast
import com.example.theweatheroracle.model.weather.ForecastEntity
import com.example.theweatheroracle.model.weather.Weather
import com.example.theweatheroracle.model.weather.WeatherEntryEntity

class FakeWeatherLocalDataSource(
    private var cities: MutableList<City>,
    private var forecastEntities: MutableList<ForecastEntity>,
    private var weatherEntries: MutableList<WeatherEntryEntity>
) : WeatherLocalDataSource {

    override suspend fun saveCity(city: City) {
        println("Saving city: ${city.name}, id: ${city.id}")
        cities.add(city)
        println("Cities after save: ${cities.map { it.name to it.id }}")
    }

    override suspend fun getCityById(cityId: Int): City? {
        return cities.find { it.id == cityId }
    }

    override suspend fun getAllCities(): List<City> {
        println("Getting all cities: ${cities.map { it.name to it.id }}")
        return cities.toList()
    }

    override suspend fun deleteCityById(cityId: Int) {
        cities.removeAll { it.id == cityId }
        val forecastIds = forecastEntities.filter { it.cityId == cityId }.map { it.forecastId }
        forecastEntities.removeAll { it.cityId == cityId }
        weatherEntries.removeAll { it.forecastId in forecastIds }
    }

    override suspend fun deleteAllCities() {
        cities.clear()
        forecastEntities.clear()
        weatherEntries.clear()
    }

    override suspend fun saveForecast(forecast: Forecast, cityId: Int) {
        val entity = ForecastEntity(forecast, cityId)
        forecastEntities.removeAll { it.forecastId == entity.forecastId && it.cityId == cityId }
        forecastEntities.add(entity)
        val savedEntity = forecastEntities.find { it.dt == entity.dt && it.cityId == cityId }
            ?: return
        forecast.weather.forEach { weather ->
            val weatherEntry = WeatherEntryEntity(weather, savedEntity.forecastId)
            weatherEntries.removeAll { it.weatherEntryId == weatherEntry.weatherEntryId && it.forecastId == savedEntity.forecastId }
            weatherEntries.add(weatherEntry)
        }
    }

    override suspend fun getForecastsForCity(cityId: Int): List<Forecast> {
        val entities = forecastEntities.filter { it.cityId == cityId }
        return entities.map { entity ->
            val weather = weatherEntries.filter { it.forecastId == entity.forecastId }
            Forecast(entity, weather)
        }
    }

    override suspend fun getForecastsForCityAndDt(cityId: Int, dt: Long): List<Forecast> {
        val entities = forecastEntities.filter { it.cityId == cityId && it.dt == dt }
        return entities.map { entity ->
            val weather = weatherEntries.filter { it.forecastId == entity.forecastId }
            Forecast(entity, weather)
        }
    }

    override suspend fun getForecastsForCityAfterDt(cityId: Int, dt: Long): List<Forecast> {
        val entities = forecastEntities.filter { it.cityId == cityId && it.dt > dt }
        return entities.map { entity ->
            val weather = weatherEntries.filter { it.forecastId == entity.forecastId }
            Forecast(entity, weather)
        }
    }

    override suspend fun getWeatherForForecast(forecast: Forecast): List<Weather> {
        val entity = forecastEntities.find {
            it.dt == forecast.dt &&
                    it.cityId == forecastEntities.find { f -> f.main == forecast.main && f.dt == forecast.dt }?.cityId
        }
        return if (entity != null) {
            weatherEntries.filter { it.forecastId == entity.forecastId }
                .map { Weather(it) }
        } else {
            emptyList()
        }
    }

    override suspend fun deleteForecastsForCity(cityId: Int) {
        val forecastIds = forecastEntities.filter { it.cityId == cityId }.map { it.forecastId }
        forecastEntities.removeAll { it.cityId == cityId }
        weatherEntries.removeAll { it.forecastId in forecastIds }
    }

    override suspend fun deleteAllForecasts() {
        forecastEntities.clear()
        weatherEntries.clear()
    }

    override suspend fun deleteForecastsBeforeDt(id: Int, dt: Long) {
        val forecastIds = forecastEntities.filter { it.cityId == id && it.dt < dt }.map { it.forecastId }
        forecastEntities.removeAll { it.cityId == id && it.dt < dt }
        weatherEntries.removeAll { it.forecastId in forecastIds }
    }
}