package com.example.theweatheroracle.model.db.weather

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.theweatheroracle.model.weather.WeatherEntryEntity

@Dao
interface WeatherEntryEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherEntry(weatherEntry: WeatherEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherEntries(weatherEntries: List<WeatherEntryEntity>)

    @Query("SELECT * FROM weather_entries WHERE forecastId = :forecastId")
    suspend fun getWeatherEntriesForForecast(forecastId: Long): List<WeatherEntryEntity>

    @Query("DELETE FROM weather_entries WHERE forecastId = :forecastId")
    suspend fun deleteWeatherEntriesForForecast(forecastId: Long)

    @Query("DELETE FROM weather_entries")
    suspend fun deleteAllWeatherEntries()
}