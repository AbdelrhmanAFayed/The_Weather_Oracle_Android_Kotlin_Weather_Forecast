package com.example.theweatheroracle.model.api

import com.example.theweatheroracle.BuildConfig
import com.example.theweatheroracle.model.WeatherForecastResponse
import com.example.theweatheroracle.model.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("forecast")
    fun getWeatherForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String = BuildConfig.OPENWEATHER_API_KEY,
        @Query("units") units: String? = "standard",
        @Query("mode") mode: String? = "json",
        @Query("cnt") cnt: Int? = null,
        @Query("lang") lang: String? = null
    ): Call<WeatherForecastResponse>

    @GET("forecast")
    fun getWeatherForecastByCityId(
        @Query("id") cityId: Int,
        @Query("appid") apiKey: String = BuildConfig.OPENWEATHER_API_KEY,
        @Query("units") units: String? = "standard",
        @Query("mode") mode: String? = "json",
        @Query("cnt") cnt: Int? = null,
        @Query("lang") lang: String? = null
    ): Call<WeatherForecastResponse>

    @GET("weather")
    fun getWeatherByLatLon(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String = BuildConfig.OPENWEATHER_API_KEY,
        @Query("units") units: String? = "standard",
        @Query("mode") mode: String? = "json",
        @Query("lang") lang: String? = null
    ): Call<WeatherResponse>

    @GET("weather")
    fun getCurrentWeatherByCityId(
        @Query("id") cityId: Int,
        @Query("appid") apiKey: String = BuildConfig.OPENWEATHER_API_KEY,
        @Query("units") units: String? = "standard",
        @Query("mode") mode: String? = "json",
        @Query("lang") lang: String? = null
    ): Call<WeatherResponse>
}