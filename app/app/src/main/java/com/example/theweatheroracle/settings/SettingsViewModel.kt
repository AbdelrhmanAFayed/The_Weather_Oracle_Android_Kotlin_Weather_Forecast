package com.example.theweatheroracle.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.theweatheroracle.model.settings.ISettingsManager



@Suppress("UNCHECKED_CAST")
class SettingsViewModelFactory(private val settingsManager: ISettingsManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(settingsManager) as T
        }
        throw IllegalArgumentException()
    }
}


class SettingsViewModel(private val settingsManager: ISettingsManager) : ViewModel() {

    // LiveData for each setting
    private val _location = MutableLiveData<String>()
    val location: LiveData<String> get() = _location

    private val _language = MutableLiveData<String>()
    val language: LiveData<String> get() = _language

    private val _temperatureUnit = MutableLiveData<String>()
    val temperatureUnit: LiveData<String> get() = _temperatureUnit

    private val _windSpeedUnit = MutableLiveData<String>()
    val windSpeedUnit: LiveData<String> get() = _windSpeedUnit

    private val _notifications = MutableLiveData<String>()
    val notifications: LiveData<String> get() = _notifications

    private val _chosenCity = MutableLiveData<String>()
    val chosenCity: LiveData<String> get() = _chosenCity

    init {
        _location.value = settingsManager.getLocation()
        _language.value = settingsManager.getLanguage()
        _temperatureUnit.value = settingsManager.getTemperatureUnit()
        _windSpeedUnit.value = settingsManager.getWindSpeedUnit()
        _notifications.value = settingsManager.getNotifications()
        _chosenCity.value = settingsManager.getChosenCity()
    }

    fun setLocation(location: String) {
        settingsManager.setLocation(location)
        _location.value = location
    }

    fun setLanguage(language: String) {
        settingsManager.setLanguage(language)
        _language.value = language
    }

    fun setTemperatureUnit(unit: String) {
        settingsManager.setTemperatureUnit(unit)
        _temperatureUnit.value = unit
    }

    fun setWindSpeedUnit(unit: String) {
        settingsManager.setWindSpeedUnit(unit)
        _windSpeedUnit.value = unit
    }

    fun setNotifications(enabled: String) {
        settingsManager.setNotifications(enabled)
        _notifications.value = enabled
    }

    fun setChosenCity(city: String) {
        settingsManager.setChosenCity(city)
        _chosenCity.value = city
    }
}