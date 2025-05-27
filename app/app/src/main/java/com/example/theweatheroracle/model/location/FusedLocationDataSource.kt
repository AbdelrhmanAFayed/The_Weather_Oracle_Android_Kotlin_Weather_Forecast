package com.example.theweatheroracle.model.location

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

class FusedLocationDataSource(
    private val context: Context,
    private val fusedClient: FusedLocationProviderClient
) : LocationDataSource {

    private var currentCallback: LocationCallback? = null

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

    override fun isLocationServiceEnabled(): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        return lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
    }

    @Suppress("MissingPermission")
    override suspend fun getLastKnownLocation(): Location? = fusedClient.lastLocation.await()

    @Suppress("MissingPermission")
    override fun requestLocationUpdates(
        intervalMs: Long,
        fastestIntervalMs: Long,
        listener: LocationDataSource.LocationListener
    ) {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    listener.onLocationUpdated(location)
                }
            }
        }
        currentCallback = callback

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            intervalMs
        ).setMinUpdateIntervalMillis(fastestIntervalMs)
            .build()

        fusedClient.requestLocationUpdates(request, callback, null)
    }

    override fun removeLocationUpdates() {
        currentCallback?.let { fusedClient.removeLocationUpdates(it) }
        currentCallback = null
    }
}

