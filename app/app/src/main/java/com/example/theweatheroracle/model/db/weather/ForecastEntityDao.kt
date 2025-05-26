package com.example.theweatheroracle.model.db.weather

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.theweatheroracle.model.weather.ForecastEntity

@Dao
interface ForecastEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecast(forecast: ForecastEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecasts(forecasts: List<ForecastEntity>): List<Long>

    @Query("SELECT * FROM forecasts WHERE cityId = :cityId")
    suspend fun getForecastsForCity(cityId: Int): List<ForecastEntity>

    @Query("SELECT * FROM forecasts WHERE cityId = :cityId AND dt = :dt")
    suspend fun getForecastsForCityAndDt(cityId: Int, dt: Long): List<ForecastEntity>

    @Query("SELECT * FROM forecasts WHERE cityId = :cityId AND dt > :dt")
    suspend fun getForecastsForCityAfterDt(cityId: Int, dt: Long): List<ForecastEntity>

    @Query("DELETE FROM forecasts WHERE cityId = :cityId AND dt < :dt")
    suspend fun deleteForecastsBeforeDt(cityId: Int, dt: Long)

    @Query("DELETE FROM forecasts WHERE cityId = :cityId")
    suspend fun deleteForecastsForCity(cityId: Int)

    @Query("DELETE FROM forecasts")
    suspend fun deleteAllForecasts()
}