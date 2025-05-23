package com.example.theweatheroracle.splash

import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.RadioButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatCheckBox
import com.airbnb.lottie.LottieAnimationView
import com.example.theweatheroracle.nav.NavActivity
import com.example.theweatheroracle.R
import com.example.theweatheroracle.map.MapSelectionDialogFragment
import com.example.theweatheroracle.model.settings.ISettingsManager
import com.example.theweatheroracle.model.settings.SettingsManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import androidx.activity.result.ActivityResultLauncher

class MainActivity : AppCompatActivity() {
    private lateinit var settingsManager: ISettingsManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var isLocationMethodGPS: Boolean = true
    private var isNotificationsEnabled: Boolean = false
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        settingsManager = SettingsManager(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (fineLocationGranted && coarseLocationGranted) {
                settingsManager.setLocationPermission(true)
                fetchLocation()
            } else {
                latitude = 29.9978
                longitude = 31.0529
                settingsManager.setLocationPermission(false)
                settingsManager.setLatitude(latitude!!)
                settingsManager.setLongitude(longitude!!)
                proceedToNavActivity()
            }
        }

        isLocationMethodGPS = settingsManager.getLocation() == "gps"
        isNotificationsEnabled = settingsManager.getNotifications() == "enable"
        latitude = settingsManager.getLatitude()
        longitude = settingsManager.getLongitude()

        val lottieAnimationView = findViewById<LottieAnimationView>(R.id.lottie_splash)
        lottieAnimationView.setAnimation(R.raw.splash)
        lottieAnimationView.playAnimation()

        lottieAnimationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                if (settingsManager.isFirstTime()) {
                    showSetupAlert()
                } else {
                    if (isLocationMethodGPS && !areLocationPermissionsGranted()) {
                        requestLocationPermissions()
                    } else {
                        proceedToNavActivity()
                    }
                }
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    private fun showSetupAlert() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_setup, null)
        val gpsRadioButton = dialogView.findViewById<RadioButton>(R.id.radio_gps)
        val mapRadioButton = dialogView.findViewById<RadioButton>(R.id.radio_map)
        val notificationsCheckBox = dialogView.findViewById<AppCompatCheckBox>(R.id.checkbox_notifications)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Welcome to Weather Oracle!")
            .setMessage("Choose your location method and notification preference.")
            .setView(dialogView)
            .setPositiveButton("Confirm") { _, _ ->
                isLocationMethodGPS = gpsRadioButton.isChecked
                isNotificationsEnabled = notificationsCheckBox.isChecked

                // Save preferences
                settingsManager.setFirstTime(false)
                settingsManager.setLocation(if (isLocationMethodGPS) "gps" else "map")
                settingsManager.setNotifications(if (isNotificationsEnabled) "enable" else "disable")

                if (isLocationMethodGPS) {
                    requestLocationPermissions()
                } else {
                    showMapOverlay()
                }
            }
            .setNegativeButton("Skip") { _, _ ->
                // Use default location (Giza, Egypt)
                latitude = 29.9978
                longitude = 31.0529
                settingsManager.setFirstTime(false)
                settingsManager.setLatitude(latitude!!)
                settingsManager.setLongitude(longitude!!)
                proceedToNavActivity()
            }
            .setCancelable(false)
            .create()

        dialog.show()
    }

    private fun showMapOverlay() {
        val mapDialog = MapSelectionDialogFragment.newInstance { lat, lon ->
            latitude = lat
            longitude = lon
            settingsManager.setLatitude(lat)
            settingsManager.setLongitude(lon)
            proceedToNavActivity()
        }
        mapDialog.show(supportFragmentManager, "MapSelectionDialog")
    }

    private fun areLocationPermissionsGranted(): Boolean {
        return (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestLocationPermissions() {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        CoroutineScope(Dispatchers.Main).launch {
            val locationResult = withContext(Dispatchers.IO) {
                try {
                    val location: Location? = fusedLocationClient.lastLocation.await()
                    if (location != null) {
                        Pair(location.latitude, location.longitude)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }

            if (locationResult != null) {
                latitude = locationResult.first
                longitude = locationResult.second
            } else {
                // Use default location (Giza, Egypt)
                latitude = 29.9978
                longitude = 31.0529
            }

            settingsManager.setLatitude(latitude!!)
            settingsManager.setLongitude(longitude!!)
            proceedToNavActivity()
        }
    }

    private fun proceedToNavActivity() {
        val intent = Intent(this@MainActivity, NavActivity::class.java).apply {
            putExtra("latitude", latitude)
            putExtra("longitude", longitude)
        }
        startActivity(intent)
        finish()
    }
}