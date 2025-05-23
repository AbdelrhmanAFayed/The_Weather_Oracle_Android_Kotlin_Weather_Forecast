package com.example.theweatheroracle.model

interface WeatherRepository {
    suspend fun fetchWeatherForecast(
        latitude: Double,
        longitude: Double,
        units: String? = "standard",
        mode: String? = "json",
        cnt: Int? = null,
        lang: String? = null
    ): Result<WeatherForecastResponse>

    suspend fun fetchForecastByCityId(
        cityId: Int,
        units: String? = "standard",
        mode: String? = "json",
        cnt: Int? = null,
        lang: String? = null
    ): Result<WeatherForecastResponse>

    suspend fun fetchWeatherByLatLon(
        latitude: Double,
        longitude: Double,
        units: String? = "standard",
        mode: String? = "json",
        lang: String? = null
    ): Result<WeatherResponse>

    suspend fun fetchWeatherByCityId(
        cityId: Int,
        units: String? = "standard",
        mode: String? = "json",
        lang: String? = null
    ): Result<WeatherResponse>

    suspend fun getCityById(cityId: Int): City?
    suspend fun getAllCities(): List<City>
    suspend fun deleteCityById(cityId: Int)
    suspend fun deleteAllCities()

    suspend fun getForecastsForCity(cityId: Int): List<Forecast>
    suspend fun getForecastsForCityAndDt(cityId: Int, dt: Long): List<Forecast>
    suspend fun getForecastsForCityAfterDt(cityId: Int, dt: Long): List<Forecast>
    suspend fun deleteForecastsForCity(cityId: Int)
    suspend fun deleteAllForecasts()
}