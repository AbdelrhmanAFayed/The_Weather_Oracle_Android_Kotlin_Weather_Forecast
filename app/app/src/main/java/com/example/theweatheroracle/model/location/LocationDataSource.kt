package com.example.theweatheroracle.model.location

import android.location.Location

interface LocationDataSource {
    suspend fun getLastKnownLocation(): Location?

    fun hasLocationPermission(): Boolean
}