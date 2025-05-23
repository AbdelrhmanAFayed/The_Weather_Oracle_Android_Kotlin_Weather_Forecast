package com.example.theweatheroracle.model.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.theweatheroracle.model.City

@Dao
interface CityDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCity(city: City)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCities(cities: List<City>)

    @Query("SELECT * FROM cities WHERE id = :cityId")
    suspend fun getCityById(cityId: Int): City?

    @Query("SELECT * FROM cities")
    suspend fun getAllCities(): List<City>

    @Query("DELETE FROM cities WHERE id = :cityId")
    suspend fun deleteCityById(cityId: Int)

    @Query("DELETE FROM cities")
    suspend fun deleteAllCities()
}