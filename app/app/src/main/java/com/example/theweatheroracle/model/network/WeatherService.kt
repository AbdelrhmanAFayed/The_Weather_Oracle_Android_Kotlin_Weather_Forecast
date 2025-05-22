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
        @Query("appid") apiKey: String = BuildConfig.OPENWEATHER_API_KEY
    ): WeatherForecastResponse

    @GET("forecast")
    suspend fun getForecastByCityId(
        @Query("id") cityId: Int,
        @Query("appid") apiKey: String = BuildConfig.OPENWEATHER_API_KEY
    ): WeatherForecastResponse

    @GET("weather")
    suspend fun getWeatherByLatLon(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String = BuildConfig.OPENWEATHER_API_KEY
    ): WeatherResponse

    @GET("weather")
    suspend fun getWeatherByCityId(
        @Query("id") cityId: Int,
        @Query("appid") apiKey: String = BuildConfig.OPENWEATHER_API_KEY
    ): WeatherResponse
}