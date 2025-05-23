package com.example.theweatheroracle.model

import android.os.Parcelable
import androidx.room.Embedded
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

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
) : Parcelable {
    constructor(entity: ForecastEntity, weatherEntries: List<WeatherEntryEntity>) : this(
        dt = entity.dt,
        main = entity.main,
        weather = weatherEntries.map { Weather(it) },
        clouds = entity.clouds,
        wind = entity.wind,
        visibility = entity.visibility,
        pop = entity.pop,
        rain = entity.rain,
        sys = entity.sys,
        dt_txt = entity.dt_txt
    )
}

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
) : Parcelable {
    constructor(entity: WeatherEntryEntity) : this(
        id = entity.id,
        main = entity.main,
        description = entity.description,
        icon = entity.icon
    )
}

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
    @SerializedName("pod") val pod: String? = null,
    @SerializedName("type") val type: Int? = null,
    @SerializedName("id") val id: Int? = null,
    @SerializedName("country") val country: String? = null,
    @SerializedName("sunrise") val sunrise: Long? = null,
    @SerializedName("sunset") val sunset: Long? = null
) : Parcelable

@Parcelize
@Entity(tableName = "cities")
data class City(
    @PrimaryKey
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @Embedded @SerializedName("coord") val coord: Coord,
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

@Parcelize
@Entity(
    tableName = "forecasts",
    foreignKeys = [
        ForeignKey(
            entity = City::class,
            parentColumns = ["id"],
            childColumns = ["cityId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ForecastEntity(
    @PrimaryKey(autoGenerate = true)
    val forecastId: Long = 0,
    val cityId: Int,
    val dt: Long,
    @Embedded val main: Main,
    @Embedded val clouds: Clouds,
    @Embedded val wind: Wind,
    val visibility: Int,
    val pop: Double,
    @Embedded val rain: Rain?,
    @Embedded val sys: Sys,
    val dt_txt: String
) : Parcelable {
    constructor(forecast: Forecast, cityId: Int) : this(
        forecastId = 0,
        cityId = cityId,
        dt = forecast.dt,
        main = forecast.main,
        clouds = forecast.clouds,
        wind = forecast.wind,
        visibility = forecast.visibility,
        pop = forecast.pop,
        rain = forecast.rain,
        sys = forecast.sys,
        dt_txt = forecast.dt_txt
    )
}

@Parcelize
@Entity(
    tableName = "weather_entries",
    foreignKeys = [
        ForeignKey(
            entity = ForecastEntity::class,
            parentColumns = ["forecastId"],
            childColumns = ["forecastId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WeatherEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val weatherEntryId: Long = 0,
    val forecastId: Long,
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
) : Parcelable {
    constructor(weather: Weather, forecastId: Long) : this(
        weatherEntryId = 0,
        forecastId = forecastId,
        id = weather.id,
        main = weather.main,
        description = weather.description,
        icon = weather.icon
    )
}