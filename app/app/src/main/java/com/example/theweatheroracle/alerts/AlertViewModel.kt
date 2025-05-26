package com.example.theweatheroracle.alerts


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.theweatheroracle.model.alert.Alert
import com.example.theweatheroracle.model.db.AppDatabase
import com.example.theweatheroracle.model.settings.ISettingsManager
import com.example.theweatheroracle.model.weather.WeatherRepository
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class AlertViewModelFactory(
    private val repository: WeatherRepository,
    private val settingsManager: ISettingsManager,
    private val appDatabase: AppDatabase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlertViewModel::class.java)) {
            return AlertViewModel(repository, settingsManager, appDatabase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


class AlertViewModel(
    private val repository: WeatherRepository,
    private val settingsManager: ISettingsManager,
    private val appDatabase: AppDatabase
) : ViewModel() {

    private val _alerts = MutableLiveData<List<Alert>>()
    val alerts: LiveData<List<Alert>> get() = _alerts

    private val _weatherData = MutableLiveData<String>()
    val weatherData: LiveData<String> get() = _weatherData

    init {
        loadAlerts()
    }

    private fun loadAlerts() {
        viewModelScope.launch {
            _alerts.value = appDatabase.alertDao().getAllAlerts()
        }
    }

    fun addAlert(alert: Alert) {
        viewModelScope.launch {
            appDatabase.alertDao().insert(alert)
            loadAlerts()
            scheduleAlert(alert)
        }
    }

    fun deleteAlert(alert: Alert) {
        viewModelScope.launch {
            appDatabase.alertDao().delete(alert)
            loadAlerts()
        }
    }

    private fun scheduleAlert(alert: Alert) {
        val currentTime = System.currentTimeMillis()
        val delay = alert.dateTime - currentTime
        if (delay > 0) {
            val data = Data.Builder()
                .putInt("alert_id", alert.id)
                .putInt("city_id", alert.cityId ?: -1)
                .putDouble("latitude", alert.latitude)
                .putDouble("longitude", alert.longitude)
                .putString("type", alert.type)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<AlertWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build()

            WorkManager.getInstance().enqueue(workRequest)
        }
    }

    fun fetchWeatherForAlert(alert: Alert) {
        viewModelScope.launch {
            val result = if (alert.cityId != null) {
                repository.fetchWeatherByCityId(
                    alert.cityId,
                    settingsManager.getTemperatureUnit(),
                    null,
                    settingsManager.getLanguage()
                )
            } else {
                repository.fetchWeatherByLatLon(
                    alert.latitude,
                    alert.longitude,
                    settingsManager.getTemperatureUnit(),
                    null, // mode
                    settingsManager.getLanguage()
                )
            }
            _weatherData.value = result.getOrNull()?.let { weather ->
                val tempUnit = settingsManager.getTemperatureUnit()
                val (convertedTemp, tempUnitLabel) = convertTemperature(weather.main.temp, tempUnit)
                "Temperature: ${String.format("%.1f %s", convertedTemp, tempUnitLabel)}"
            } ?: "Weather data unavailable"
        }
    }

    private fun convertTemperature(kelvin: Double, unit: String): Pair<Double, String> {
        return when (unit.lowercase()) {
            "celsius" -> (kelvin - 273.15) to "°C"
            "fahrenheit" -> ((kelvin - 273.15) * 9 / 5 + 32) to "°F"
            else -> kelvin to "K"
        }
    }
}