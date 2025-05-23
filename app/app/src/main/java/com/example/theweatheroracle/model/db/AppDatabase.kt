package com.example.theweatheroracle.model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.theweatheroracle.model.City
import com.example.theweatheroracle.model.ForecastEntity
import com.example.theweatheroracle.model.WeatherEntryEntity

@Database(entities = [City::class, ForecastEntity::class, WeatherEntryEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cityDao(): CityDao
    abstract fun forecastEntityDao(): ForecastEntityDao
    abstract fun weatherEntryEntityDao(): WeatherEntryEntityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "weather_oracle_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}