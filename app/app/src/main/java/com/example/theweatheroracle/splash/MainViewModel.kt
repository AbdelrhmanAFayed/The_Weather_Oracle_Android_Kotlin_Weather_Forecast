package com.example.theweatheroracle.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.theweatheroracle.model.location.LocationDataSource
import com.example.theweatheroracle.model.settings.ISettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainViewModelFactory(
    private val settingsManager: ISettingsManager,
    private val locationDataSource: LocationDataSource
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(settingsManager, locationDataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


class MainViewModel(
    private val settingsManager: ISettingsManager,
    private val locationDataSource: LocationDataSource
) : ViewModel() {

    private val _isFirstTime = MutableLiveData<Boolean>()
    val isFirstTime: LiveData<Boolean> = _isFirstTime

    private val _useGPS = MutableLiveData<Boolean>()
    val useGPS: LiveData<Boolean> = _useGPS

    private val _notificationsEnabled = MutableLiveData<Boolean>()
    val notificationsEnabled: LiveData<Boolean> = _notificationsEnabled

    private val _latitude = MutableLiveData<Double?>()
    val latitude: LiveData<Double?> = _latitude

    private val _longitude = MutableLiveData<Double?>()
    val longitude: LiveData<Double?> = _longitude

    private val _uiEvent = MutableLiveData<UiEvent>()
    val uiEvent: LiveData<UiEvent> = _uiEvent

    sealed class UiEvent {
        object ShowSetupDialog : UiEvent()
        object ShowEnableLocationServices   : UiEvent()
        object RequestLocationPermission : UiEvent()
        object LaunchMapPicker : UiEvent()
        data class NavigateToNav(val lat: Double, val lon: Double) : UiEvent()
        data class ShowError(val message: String) : UiEvent()
    }

    init {
        loadInitialState()
    }
    private fun loadInitialState() {
        _isFirstTime.value = settingsManager.isFirstTime()
        _useGPS.value = settingsManager.getLocation() == "gps"
        _notificationsEnabled.value = settingsManager.getNotifications() == "enable"
        _latitude.value = settingsManager.getLatitude()
        _longitude.value = settingsManager.getLongitude()
    }

    fun onSplashEnd() {
        if (settingsManager.isFirstTime()) {
            _isFirstTime.value = settingsManager.isFirstTime()
            _uiEvent.value = UiEvent.ShowSetupDialog
            return
        }

        if (useGPS.value == true) {
            if (!locationDataSource.hasLocationPermission()) {
                _uiEvent.value = UiEvent.RequestLocationPermission
            } else {
                proceedWithLocation()
            }
        } else {
            val lat = latitude.value
            val lon = longitude.value
            if (lat != null && lon != null) {
                saveAndNavigate(lat, lon)
            } else {
                _uiEvent.value = UiEvent.LaunchMapPicker
            }
        }
    }

    fun onSetupConfirmed(useGps: Boolean, notifyEnabled: Boolean) {
        settingsManager.setFirstTime(false)
        _isFirstTime.value = false
        settingsManager.setLocation(if (useGps) "gps" else "map")
        settingsManager.setNotifications(if (notifyEnabled) "enable" else "disable")
        _useGPS.value = useGps
        _notificationsEnabled.value = notifyEnabled

        onSplashEnd()
    }

    fun onPermissionsResult(granted: Boolean) {
        if (granted) {
            proceedWithLocation()
        } else {
            _uiEvent.value = UiEvent.ShowError("Location permission denied.")
            settingsManager.setLocation("map")
        }
    }

    fun onMapPicked(lat: Double, lon: Double) {
        saveAndNavigate(lat, lon)
    }

    fun proceedWithLocation() {
        viewModelScope.launch {
            try {
                if (!locationDataSource.isLocationServiceEnabled()) {
                    _uiEvent.value = UiEvent.ShowEnableLocationServices
                    return@launch
                }

                val loc = withContext(Dispatchers.IO) {
                    locationDataSource.getLastKnownLocation()
                }

                if (loc != null) {
                    saveAndNavigate(loc.latitude, loc.longitude)
                } else {
                    _uiEvent.value = UiEvent.LaunchMapPicker
                }

            } catch (e: Exception) {
                _uiEvent.value = UiEvent.ShowError("Failed to fetch location: ${e.message}")
            }
        }
    }

    private fun saveAndNavigate(lat: Double, lon: Double) {
        settingsManager.setLatitude(lat)
        settingsManager.setLongitude(lon)
        _latitude.value = lat
        _longitude.value = lon
        _uiEvent.value = UiEvent.NavigateToNav(lat, lon)
    }

    fun onLocationServicesDeclined() {
        settingsManager.setLocation("map")
        _uiEvent.value = UiEvent.LaunchMapPicker
    }
}