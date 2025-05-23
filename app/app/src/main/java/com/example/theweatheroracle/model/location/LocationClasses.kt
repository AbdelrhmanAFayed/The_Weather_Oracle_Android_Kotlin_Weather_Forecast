package com.example.theweatheroracle.model.location

data class LocationData(val latitude: Double, val longitude: Double)

enum class LocationMethod { GPS, MAP }

open class LocationResult {
    data class Success(val locationData: LocationData) : LocationResult()
    data class Failure(val exception: Throwable) : LocationResult()
}
