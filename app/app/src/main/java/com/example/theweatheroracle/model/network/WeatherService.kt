package com.example.theweatheroracle.model.network

import com.example.theweatheroracle.BuildConfig
import com.example.theweatheroracle.model.WeatherForecastResponse
import com.example.theweatheroracle.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("forecast")
    suspend fun getWeatherForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String = BuildConfig.OPENWEATHER_API_KEY,
        @Query("units") units: String? = "standard",
        @Query("mode") mode: String? = "json",
        @Query("cnt") cnt: Int? = null,
        @Query("lang") lang: String? = null
    ): WeatherForecastResponse

    @GET("forecast")
    suspend fun getWeatherForecastByCityId(
        @Query("id") cityId: Int,
        @Query("appid") apiKey: String = BuildConfig.OPENWEATHER_API_KEY,
        @Query("units") units: String? = "standard",
        @Query("mode") mode: String? = "json",
        @Query("cnt") cnt: Int? = null,
        @Query("lang") lang: String? = null
    ): WeatherForecastResponse

    @GET("weather")
    suspend fun getWeatherByLatLon(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String = BuildConfig.OPENWEATHER_API_KEY,
        @Query("units") units: String? = "standard",
        @Query("mode") mode: String? = "json",
        @Query("lang") lang: String? = null
    ): WeatherResponse

    @GET("weather")
    suspend fun getCurrentWeatherByCityId(
        @Query("id") cityId: Int,
        @Query("appid") apiKey: String = BuildConfig.OPENWEATHER_API_KEY,
        @Query("units") units: String? = "standard",
        @Query("mode") mode: String? = "json",
        @Query("lang") lang: String? = null
    ): WeatherResponse
}