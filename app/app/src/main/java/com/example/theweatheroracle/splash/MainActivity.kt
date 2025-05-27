package com.example.theweatheroracle.splash

import android.Manifest
import android.animation.Animator
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.theweatheroracle.R
import com.example.theweatheroracle.databinding.ActivityMainBinding
import com.example.theweatheroracle.databinding.DialogSetupBinding
import com.example.theweatheroracle.lan.LocaleUtils
import com.example.theweatheroracle.map.MapSelectionDialogFragment
import com.example.theweatheroracle.model.location.FusedLocationDataSource
import com.example.theweatheroracle.nav.NavActivity
import com.example.theweatheroracle.model.settings.SettingsManager
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var locationSettingsLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleUtils.updateLocale(this)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = MainViewModelFactory(
            SettingsManager(this),
            FusedLocationDataSource(this, LocationServices.getFusedLocationProviderClient(this))
        )
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        setupPermissionLauncher()
        setupLocationSettingsLauncher()
        observeUiEvents()

        binding.lottieSplash.setAnimation(R.raw.splash)
        binding.lottieSplash.playAnimation()
        binding.lottieSplash.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator) {
                viewModel.onSplashEnd()
            }
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    private fun setupPermissionLauncher() {
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { perms ->
            val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            viewModel.onPermissionsResult(granted)
        }
    }

    private fun observeUiEvents() {
        viewModel.uiEvent.observe(this) { event ->
            when (event) {
                is MainViewModel.UiEvent.ShowSetupDialog ->
                    showSetupDialog()
                is MainViewModel.UiEvent.RequestLocationPermission ->
                    requestLocationPermissions()
                is MainViewModel.UiEvent.LaunchMapPicker ->
                    showMapOverlay()
                is MainViewModel.UiEvent.NavigateToNav ->
                    navigateToNav(event.lat, event.lon)
                is MainViewModel.UiEvent.ShowError ->
                    showErrorDialog(event.message)
                is MainViewModel.UiEvent.ShowEnableLocationServices ->
                    showEnableLocationServicesDialog()
            }
        }
    }

    private fun requestLocationPermissions() {
        permissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }
    private fun setupLocationSettingsLauncher() {
        locationSettingsLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
                viewModel.proceedWithLocation()
            }
    }

    private fun showSetupDialog() {
        val binding = DialogSetupBinding.inflate(layoutInflater)

        AlertDialog.Builder(this)
            .setTitle("Welcome to Weather Oracle!")
            .setMessage("Choose your location method and notifications.")
            .setView(binding.root)
            .setPositiveButton("Confirm") { _, _ ->
                viewModel.onSetupConfirmed(
                    useGps = binding.radioGps.isChecked,
                    notifyEnabled = binding.checkboxNotifications.isChecked
                )
            }
            .setNegativeButton("Skip") { _, _ ->
                viewModel.onSetupConfirmed(
                    useGps = false,
                    notifyEnabled = false
                )
            }
            .setCancelable(false)
            .show()
    }

    private fun showMapOverlay() {
        val dialog = MapSelectionDialogFragment { lat, lon ->
            viewModel.onMapPicked(lat, lon)
        }
        dialog.setCancelable(false)
        dialog.show(supportFragmentManager, "MapSelection")
    }

    private fun navigateToNav(lat: Double, lon: Double) {
        startActivity(Intent(this, NavActivity::class.java).apply {
            putExtra("latitude", lat)
            putExtra("longitude", lon)
        })
        finish()
    }

    private fun showEnableLocationServicesDialog() {
        AlertDialog.Builder(this)
            .setTitle("Location Services Are Off")
            .setMessage("Please enable GPS or network location in your device settings to continue.")
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                locationSettingsLauncher.launch(intent)
            }
            .setNegativeButton("Cancel") { _, _ ->
                viewModel.onLocationServicesDeclined()
            }
            .setCancelable(false)
            .show()
    }
    private fun showErrorDialog(msg: String) {

        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(msg)
            .setPositiveButton("OK", object : DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    if(msg == "Location permission denied.")
                    {
                        this@MainActivity.viewModel.onLocationServicesDeclined()
                    }
                }

            })
            .show()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleUtils.updateLocale(this)
    }

}
