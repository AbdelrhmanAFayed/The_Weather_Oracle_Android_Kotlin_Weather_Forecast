package com.example.theweatheroracle.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class WeatherForecastResponse(
    val cod: String,
    val message: Int,
    val cnt: Int,
    val list: List<Forecast>,
    val city: City
) : Parcelable

@Parcelize
data class Forecast(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>,
    val clouds: Clouds,
    val wind: Wind,
    val visibility: Int,
    val pop: Double,
    val rain: Rain?,
    val sys: Sys,
    val dt_txt: String
) : Parcelable

@Parcelize
data class WeatherResponse(
    @SerializedName("coord") val coord: Coord,
    @SerializedName("weather") val weather: List<Weather>,
    @SerializedName("base") val base: String,
    @SerializedName("main") val main: Main,
    @SerializedName("wind") val wind: Wind,
    @SerializedName("clouds") val clouds: Clouds,
    @SerializedName("visibility") val visibility: Int,
    @SerializedName("rain") val rain: CurrentRain?,
    @SerializedName("dt") val dt: Long,
    @SerializedName("sys") val sys: Sys,
    @SerializedName("timezone") val timezone: Int,
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("cod") val cod: Int
) : Parcelable

@Parcelize
data class Main(
    @SerializedName("temp") val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double,
    @SerializedName("pressure") val pressure: Int,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("sea_level") val seaLevel: Int? = null,
    @SerializedName("grnd_level") val grndLevel: Int? = null,
    @SerializedName("temp_kf") val tempKf: Double? = null
) : Parcelable

@Parcelize
data class Weather(
    @SerializedName("id") val id: Int,
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
) : Parcelable

@Parcelize
data class Clouds(
    @SerializedName("all") val all: Int
) : Parcelable

@Parcelize
data class Wind(
    @SerializedName("speed") val speed: Double,
    @SerializedName("deg") val deg: Int,
    @SerializedName("gust") val gust: Double? = null
) : Parcelable

@Parcelize
data class Rain(
    @SerializedName("3h") val threeHours: Double
) : Parcelable

@Parcelize
data class CurrentRain(
    @SerializedName("1h") val oneHour: Double?
) : Parcelable

@Parcelize
data class Sys(
    @SerializedName("pod") val pod: String? = null, // For /forecast
    @SerializedName("type") val type: Int? = null, // For /weather
    @SerializedName("id") val id: Int? = null, // For /weather
    @SerializedName("country") val country: String? = null, // For /weather
    @SerializedName("sunrise") val sunrise: Long? = null, // For /weather
    @SerializedName("sunset") val sunset: Long? = null // For /weather
) : Parcelable

@Parcelize
data class City(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("coord") val coord: Coord,
    @SerializedName("country") val country: String,
    @SerializedName("population") val population: Int,
    @SerializedName("timezone") val timezone: Int,
    @SerializedName("sunrise") val sunrise: Long,
    @SerializedName("sunset") val sunset: Long
) : Parcelable

@Parcelize
data class Coord(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double
) : Parcelable