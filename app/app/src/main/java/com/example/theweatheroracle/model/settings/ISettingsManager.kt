package com.example.theweatheroracle.model.settings

interface ISettingsManager {
    fun isFirstTime(): Boolean
    fun setFirstTime(isFirst: Boolean)

    fun hasLocationPermission(): Boolean
    fun setLocationPermission(hasPermission: Boolean)

    fun getLocation(): String
    fun setLocation(location: String)

    fun getLanguage(): String
    fun setLanguage(language: String)

    fun getTemperatureUnit(): String
    fun setTemperatureUnit(unit: String)

    fun getWindSpeedUnit(): String
    fun setWindSpeedUnit(unit: String)

    fun getNotifications(): String
    fun setNotifications(enabled: String)

    fun getChosenCity(): String
    fun setChosenCity(city: String)

    fun getLatitude(): Double?
    fun setLatitude(latitude: Double)

    fun getLongitude(): Double?
    fun setLongitude(longitude: Double)

}