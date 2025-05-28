package com.example.theweatheroracle.settings

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.theweatheroracle.R
import com.example.theweatheroracle.databinding.ActivitySettingsBinding
import com.example.theweatheroracle.lan.LocaleUtils
import com.example.theweatheroracle.model.settings.SettingsManager
import com.example.theweatheroracle.splash.MainActivity

class Settings : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val settingsViewModel: SettingsViewModel by lazy {
        ViewModelProvider(this, SettingsViewModelFactory(SettingsManager(this)))[SettingsViewModel::class.java]
    }
    private var isInitialSetup = true
    private var isDialogVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleUtils.updateLocale(this)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingsViewModel.location.observe(this) { location ->
            binding.locationRadioGroup.check(
                when (location) {
                    "gps" -> R.id.location_gps_radio
                    "map" -> R.id.location_map_radio
                    else -> R.id.location_gps_radio
                }, suppressListener = true
            )
        }

        settingsViewModel.language.observe(this) { language ->
            binding.languageRadioGroup.check(
                when (language) {
                    "system" -> R.id.language_system_radio
                    "english" -> R.id.language_english_radio
                    "arabic" -> R.id.language_arabic_radio
                    else -> R.id.language_system_radio
                }, suppressListener = true
            )
            if (isInitialSetup) {
                isInitialSetup = false
                setupListeners()
            }
        }

        settingsViewModel.temperatureUnit.observe(this) { unit ->
            binding.temperatureRadioGroup.check(
                when (unit) {
                    "celsius" -> R.id.temperature_celsius_radio
                    "kelvin" -> R.id.temperature_kelvin_radio
                    "fahrenheit" -> R.id.temperature_fahrenheit_radio
                    else -> R.id.temperature_celsius_radio
                }, suppressListener = false
            )
        }

        settingsViewModel.windSpeedUnit.observe(this) { unit ->
            binding.windSpeedRadioGroup.check(
                when (unit) {
                    "ms" -> R.id.wind_speed_ms_radio
                    "mph" -> R.id.wind_speed_mph_radio
                    else -> R.id.wind_speed_ms_radio
                }, suppressListener = false
            )
        }

        settingsViewModel.notifications.observe(this) { enabled ->
            binding.notificationsRadioGroup.check(
                when (enabled) {
                    "enable" -> R.id.notifications_enable_radio
                    "disable" -> R.id.notifications_disable_radio
                    else -> R.id.notifications_enable_radio
                }, suppressListener = false
            )
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleUtils.updateLocale(this)
    }

    private fun setupListeners() {
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
            if (!isInitialSetup && !isDialogVisible) {
                val newLanguage = when (checkedId) {
                    R.id.language_system_radio -> "system"
                    R.id.language_english_radio -> "english"
                    R.id.language_arabic_radio -> "arabic"
                    else -> "system"
                }
                showLanguageConfirmationDialog(newLanguage)
            }
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

    private fun showLanguageConfirmationDialog(newLanguage: String) {
        if (isDialogVisible) return
        isDialogVisible = true

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.confirm_language_change))
            .setMessage(getString(R.string.changing_the_language_will_restart_the_app_proceed))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                settingsViewModel.setLanguage(newLanguage)
                isDialogVisible = false
                restartApp()
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                settingsViewModel.language.value?.let { currentLanguage ->
                    binding.languageRadioGroup.check(
                        when (currentLanguage) {
                            "system" -> R.id.language_system_radio
                            "english" -> R.id.language_english_radio
                            "arabic" -> R.id.language_arabic_radio
                            else -> R.id.language_system_radio
                        }, suppressListener = true
                    )
                }
                isDialogVisible = false
                dialog.dismiss()
            }
            .setCancelable(false)
            .setOnDismissListener { isDialogVisible = false }
            .show()
    }

    private fun restartApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun RadioGroup.check(id: Int, suppressListener: Boolean) {
        if (suppressListener) {
            setOnCheckedChangeListener(null)
            check(id)
            setupListeners()
        } else {
            check(id)
        }
    }
}