package com.example.theweatheroracle.settings

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.theweatheroracle.R
import com.example.theweatheroracle.databinding.ActivitySettingsBinding
import com.example.theweatheroracle.model.settings.SettingsManager

class Settings : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val settingsViewModel: SettingsViewModel by lazy {
        ViewModelProvider(this, SettingsViewModelFactory(SettingsManager(this))).get(SettingsViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        settingsViewModel.location.observe(this) { location ->
            when (location) {
                "gps" -> binding.locationRadioGroup.check(R.id.location_gps_radio)
                "map" -> binding.locationRadioGroup.check(R.id.location_map_radio)
            }
        }
        settingsViewModel.language.observe(this) { language ->
            when (language) {
                "english" -> binding.languageRadioGroup.check(R.id.language_english_radio)
                "arabic" -> binding.languageRadioGroup.check(R.id.language_arabic_radio)
            }
        }
        settingsViewModel.temperatureUnit.observe(this) { unit ->
            when (unit) {
                "celsius" -> binding.temperatureRadioGroup.check(R.id.temperature_celsius_radio)
                "kelvin" -> binding.temperatureRadioGroup.check(R.id.temperature_kelvin_radio)
                "fahrenheit" -> binding.temperatureRadioGroup.check(R.id.temperature_fahrenheit_radio)
            }
        }
        settingsViewModel.windSpeedUnit.observe(this) { unit ->
            when (unit) {
                "ms" -> binding.windSpeedRadioGroup.check(R.id.wind_speed_ms_radio)
                "mph" -> binding.windSpeedRadioGroup.check(R.id.wind_speed_mph_radio)
            }
        }
        settingsViewModel.notifications.observe(this) { enabled ->
            when (enabled) {
                "enable" -> binding.notificationsRadioGroup.check(R.id.notifications_enable_radio)
                "disable" -> binding.notificationsRadioGroup.check(R.id.notifications_disable_radio)
            }
        }


        binding.locationRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            settingsViewModel.setLocation(
                when (checkedId) {
                    R.id.location_gps_radio -> "gps"
                    R.id.location_map_radio -> "map"
                    else -> "gps"
                }
            )
        }
        binding.languageRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            settingsViewModel.setLanguage(
                when (checkedId) {
                    R.id.language_english_radio -> "english"
                    R.id.language_arabic_radio -> "arabic"
                    else -> "english"
                }
            )
        }
        binding.temperatureRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            settingsViewModel.setTemperatureUnit(
                when (checkedId) {
                    R.id.temperature_celsius_radio -> "celsius"
                    R.id.temperature_kelvin_radio -> "kelvin"
                    R.id.temperature_fahrenheit_radio -> "fahrenheit"
                    else -> "celsius"
                }
            )
        }
        binding.windSpeedRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            settingsViewModel.setWindSpeedUnit(
                when (checkedId) {
                    R.id.wind_speed_ms_radio -> "ms"
                    R.id.wind_speed_mph_radio -> "mph"
                    else -> "ms"
                }
            )
        }
        binding.notificationsRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            settingsViewModel.setNotifications(
                when (checkedId) {
                    R.id.notifications_enable_radio -> "enable"
                    R.id.notifications_disable_radio -> "disable"
                    else -> "enable"
                }
            )
        }
    }
}