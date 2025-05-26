package com.example.theweatheroracle.model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.theweatheroracle.model.alert.Alert
import com.example.theweatheroracle.model.db.alert.AlertDao
import com.example.theweatheroracle.model.db.weather.CityDao
import com.example.theweatheroracle.model.db.weather.ForecastEntityDao
import com.example.theweatheroracle.model.db.weather.WeatherEntryEntityDao
import com.example.theweatheroracle.model.weather.City
import com.example.theweatheroracle.model.weather.ForecastEntity
import com.example.theweatheroracle.model.weather.WeatherEntryEntity

@Database(
    entities = [City::class, ForecastEntity::class, WeatherEntryEntity::class, Alert::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cityDao(): CityDao
    abstract fun forecastEntityDao(): ForecastEntityDao
    abstract fun weatherEntryEntityDao(): WeatherEntryEntityDao
    abstract fun alertDao(): AlertDao

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