package com.example.theweatheroracle.model.location

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.tasks.await

class FusedLocationDataSource(
    private val context: Context,
    private val fusedClient: FusedLocationProviderClient
) : LocationDataSource {
    override fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine && coarse
    }

    @Suppress("MissingPermission")
    override suspend fun getLastKnownLocation(): Location? =
        fusedClient.lastLocation.await()
}
