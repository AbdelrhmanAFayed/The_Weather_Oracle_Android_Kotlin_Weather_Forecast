package com.example.theweatheroracle.model.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SettingsManager(private val context: Context) : ISettingsManager {
    companion object {
        private const val PREFS_NAME = "WeatherOraclePrefs"
        private const val KEY_FIRST_TIME = "first_time"
        private const val KEY_LOCATION_PERMISSION = "location_permission"
        private const val KEY_LOCATION = "location"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_TEMPERATURE = "temperature"
        private const val KEY_WIND_SPEED = "wind_speed"
        private const val KEY_NOTIFICATIONS = "notifications"
        private const val KEY_CHOSEN_CITY = "chosen_city"

        private const val DEFAULT_FIRST_TIME = true
        private const val DEFAULT_LOCATION_PERMISSION = false
        private const val DEFAULT_LOCATION = "gps"
        private const val DEFAULT_LANGUAGE = "english"
        private const val DEFAULT_TEMPERATURE = "celsius"
        private const val DEFAULT_WIND_SPEED = "ms"
        private const val DEFAULT_NOTIFICATIONS = "enable"
        private const val DEFAULT_CHOSEN_CITY = ""
    }

    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun isFirstTime(): Boolean = preferences.getBoolean(KEY_FIRST_TIME, DEFAULT_FIRST_TIME)
    override fun setFirstTime(isFirst: Boolean) {
        preferences.edit { putBoolean(KEY_FIRST_TIME, isFirst) }
    }

    override fun hasLocationPermission(): Boolean = preferences.getBoolean(KEY_LOCATION_PERMISSION, DEFAULT_LOCATION_PERMISSION)
    override fun setLocationPermission(hasPermission: Boolean) {
        preferences.edit { putBoolean(KEY_LOCATION_PERMISSION, hasPermission) }
    }

    override fun getLocation(): String = preferences.getString(KEY_LOCATION, DEFAULT_LOCATION) ?: DEFAULT_LOCATION
    override fun setLocation(location: String) {
        preferences.edit { putString(KEY_LOCATION, location) }
    }

    override fun getLanguage(): String = preferences.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    override fun setLanguage(language: String) {
        preferences.edit { putString(KEY_LANGUAGE, language) }
    }

    override fun getTemperatureUnit(): String = preferences.getString(KEY_TEMPERATURE, DEFAULT_TEMPERATURE) ?: DEFAULT_TEMPERATURE
    override fun setTemperatureUnit(unit: String) {
        preferences.edit { putString(KEY_TEMPERATURE, unit) }
    }

    override fun getWindSpeedUnit(): String = preferences.getString(KEY_WIND_SPEED, DEFAULT_WIND_SPEED) ?: DEFAULT_WIND_SPEED
    override fun setWindSpeedUnit(unit: String) {
        preferences.edit { putString(KEY_WIND_SPEED, unit) }
    }

    override fun getNotifications(): String = preferences.getString(KEY_NOTIFICATIONS, DEFAULT_NOTIFICATIONS) ?: DEFAULT_NOTIFICATIONS
    override fun setNotifications(enabled: String) {
        preferences.edit { putString(KEY_NOTIFICATIONS, enabled) }
    }

    override fun getChosenCity(): String = preferences.getString(KEY_CHOSEN_CITY, DEFAULT_CHOSEN_CITY) ?: DEFAULT_CHOSEN_CITY
    override fun setChosenCity(city: String) {
        preferences.edit { putString(KEY_CHOSEN_CITY, city) }
    }
}