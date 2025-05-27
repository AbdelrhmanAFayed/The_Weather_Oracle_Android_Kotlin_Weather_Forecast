package com.example.theweatheroracle.model.location

import android.location.Location

interface LocationDataSource {
    suspend fun getLastKnownLocation(): Location?

    fun hasLocationPermission(): Boolean

    fun isLocationServiceEnabled(): Boolean

    fun interface LocationListener {
        fun onLocationUpdated(location: Location)
    }

    fun requestLocationUpdates(
        intervalMs: Long,
        fastestIntervalMs: Long,
        listener: LocationListener
    )

    fun removeLocationUpdates()
}