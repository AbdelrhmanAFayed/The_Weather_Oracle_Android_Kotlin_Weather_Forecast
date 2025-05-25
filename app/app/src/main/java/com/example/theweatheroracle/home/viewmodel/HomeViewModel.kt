package com.example.theweatheroracle.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.theweatheroracle.home.view.DailySummary
import com.example.theweatheroracle.model.City
import com.example.theweatheroracle.model.CurrentRain
import com.example.theweatheroracle.model.Forecast
import com.example.theweatheroracle.model.WeatherForecastResponse
import com.example.theweatheroracle.model.WeatherResponse
import com.example.theweatheroracle.model.WeatherRepository
import com.example.theweatheroracle.model.settings.ISettingsManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class HomeViewModelFactory(
    private val repository: WeatherRepository,
    private val settingsManager: ISettingsManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repository, settingsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class HomeViewModel(
    private val repository: WeatherRepository,
    private val settingsManager: ISettingsManager
) : ViewModel() {
    private val _city = MutableLiveData<City?>()
    val city: LiveData<City?> = _city

    private val _weather = MutableLiveData<WeatherResponse?>()
    val weather: LiveData<WeatherResponse?> = _weather

    private val _dailyForecasts = MutableLiveData<List<Forecast>>()
    val dailyForecasts: LiveData<List<Forecast>> = _dailyForecasts

    private val _weeklySummaries = MutableLiveData<List<DailySummary>>()
    val weeklySummaries: LiveData<List<DailySummary>> = _weeklySummaries

    private val _lastUpdated = MutableLiveData<String?>()
    val lastUpdated: LiveData<String?> = _lastUpdated

    private fun updateLastUpdatedTimestamp(timestamp: Long) {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        _lastUpdated.postValue(sdf.format(Date(timestamp * 1000)))
    }

    fun refreshData(latitude: Double, longitude: Double, cityId: Int?, useGps: Boolean, isOnline: Boolean) {
        viewModelScope.launch {
            if (useGps || cityId == null || cityId == 0) {
                fetchWeatherData(latitude, longitude, isOnline)
            } else {
                if (isOnline) {
                    fetchWeatherByCityId(cityId)
                } else {
                    fetchWeatherByCityIdFromDb(cityId)
                }
            }
        }
    }

    fun fetchWeatherData(latitude: Double, longitude: Double, isOnline: Boolean) {
        viewModelScope.launch {
            val forecastResult = repository.fetchWeatherForecast(latitude, longitude)
            forecastResult.onSuccess { forecastResponse ->
                _city.postValue(forecastResponse.city)
                _dailyForecasts.postValue(forecastResponse.list)
                _weeklySummaries.postValue(computeWeeklySummaries(forecastResponse.list))
                // Use the timestamp of the first forecast or current time
                val timestamp = forecastResponse.list.firstOrNull()?.dt ?: (System.currentTimeMillis() / 1000)
                updateLastUpdatedTimestamp(timestamp)
            }.onFailure {
                _city.postValue(null)
                _dailyForecasts.postValue(emptyList())
                _weeklySummaries.postValue(emptyList())
                _lastUpdated.postValue(null)
            }

            if (isOnline) {
                val weatherResult = repository.fetchWeatherByLatLon(latitude, longitude)
                if (weatherResult.isSuccess) {
                    weatherResult.onSuccess { weatherResponse ->
                        _weather.postValue(weatherResponse)
                        _city.postValue(
                            City(
                                id = weatherResponse.id,
                                name = weatherResponse.name,
                                coord = weatherResponse.coord,
                                country = weatherResponse.sys.country ?: "",
                                population = 0,
                                timezone = weatherResponse.timezone,
                                sunrise = weatherResponse.sys.sunrise ?: 0,
                                sunset = weatherResponse.sys.sunset ?: 0
                            )
                        )
                        updateLastUpdatedTimestamp(weatherResponse.dt)
                    }
                } else {
                    val city = _city.value
                    if (city != null) {
                        val currentDt = System.currentTimeMillis() / 1000
                        val forecasts = repository.getForecastsForCityAfterDt(city.id, currentDt)
                        val latestForecast = forecasts.minByOrNull { it.dt }
                        _weather.postValue(latestForecast?.toWeatherResponse(city))
                        latestForecast?.let { updateLastUpdatedTimestamp(it.dt) }
                    } else {
                        _weather.postValue(null)
                        _lastUpdated.postValue(null)
                    }
                }
            } else {
                val city = _city.value
                if (city != null) {
                    val currentDt = System.currentTimeMillis() / 1000
                    val forecasts = repository.getForecastsForCityAfterDt(city.id, currentDt)
                    val latestForecast = forecasts.minByOrNull { it.dt }
                    _weather.postValue(latestForecast?.toWeatherResponse(city))
                    latestForecast?.let { updateLastUpdatedTimestamp(it.dt) }
                } else {
                    _weather.postValue(null)
                    _lastUpdated.postValue(null)
                }
            }
        }
    }

    fun fetchWeatherByCityId(cityId: Int) {
        viewModelScope.launch {
            val forecastResult = repository.fetchForecastByCityId(cityId)
            forecastResult.onSuccess { forecastResponse ->
                _city.postValue(forecastResponse.city)
                _dailyForecasts.postValue(forecastResponse.list)
                _weeklySummaries.postValue(computeWeeklySummaries(forecastResponse.list))
                val timestamp = forecastResponse.list.firstOrNull()?.dt ?: (System.currentTimeMillis() / 1000)
                updateLastUpdatedTimestamp(timestamp)
            }.onFailure {
                _city.postValue(null)
                _dailyForecasts.postValue(emptyList())
                _weeklySummaries.postValue(emptyList())
                _lastUpdated.postValue(null)
            }

            val weatherResult = repository.fetchWeatherByCityId(cityId)
            if (weatherResult.isSuccess) {
                weatherResult.onSuccess { weatherResponse ->
                    _weather.postValue(weatherResponse)
                    _city.postValue(
                        City(
                            id = weatherResponse.id,
                            name = weatherResponse.name,
                            coord = weatherResponse.coord,
                            country = weatherResponse.sys.country ?: "",
                            population = 0,
                            timezone = weatherResponse.timezone,
                            sunrise = weatherResponse.sys.sunrise ?: 0,
                            sunset = weatherResponse.sys.sunset ?: 0
                        )
                    )
                    updateLastUpdatedTimestamp(weatherResponse.dt)
                }
            } else {
                val city = _city.value
                if (city != null) {
                    val currentDt = System.currentTimeMillis() / 1000
                    val forecasts = repository.getForecastsForCityAfterDt(city.id, currentDt)
                    val latestForecast = forecasts.minByOrNull { it.dt }
                    _weather.postValue(latestForecast?.toWeatherResponse(city))
                    latestForecast?.let { updateLastUpdatedTimestamp(it.dt) }
                } else {
                    _weather.postValue(null)
                    _lastUpdated.postValue(null)
                }
            }
        }
    }

    fun fetchWeatherByCityIdFromDb(cityId: Int) {
        viewModelScope.launch {
            val city = repository.getCityById(cityId)
            if (city != null) {
                _city.postValue(city)
                val forecasts = repository.getForecastsForCity(cityId)
                _dailyForecasts.postValue(forecasts)
                _weeklySummaries.postValue(computeWeeklySummaries(forecasts))

                val currentDt = System.currentTimeMillis() / 1000
                val futureForecasts = repository.getForecastsForCityAfterDt(cityId, currentDt)
                val latestForecast = futureForecasts.minByOrNull { it.dt }
                _weather.postValue(latestForecast?.toWeatherResponse(city))
                latestForecast?.let { updateLastUpdatedTimestamp(it.dt) }
            } else {
                _city.postValue(null)
                _dailyForecasts.postValue(emptyList())
                _weeklySummaries.postValue(emptyList())
                _weather.postValue(null)
                _lastUpdated.postValue(null)
            }
        }
    }

    fun cleanOldForecasts(cityId: Int) {
        viewModelScope.launch {
            repository.deleteForecastsForCityBeforeDt(
                cityId,
                Calendar.getInstance().timeInMillis / 1000
            )
        }
    }

    private fun computeWeeklySummaries(forecasts: List<Forecast>): List<DailySummary> {
        val dailyGroups = forecasts.groupBy { forecast ->
            val date = Date(forecast.dt * 1000L)
            SimpleDateFormat("EEEE", Locale.getDefault()).apply {
                timeZone = TimeZone.getDefault()
            }.format(date)
        }

        return dailyGroups.map { (day, forecastsForDay) ->
            val minTemp = forecastsForDay.minOf { it.main.tempMin }
            val maxTemp = forecastsForDay.maxOf { it.main.tempMax }.toString() + "K"
            val icon = forecastsForDay.first().weather.firstOrNull()?.icon ?: "01d"
            val description = forecastsForDay.first().weather.firstOrNull()?.description ?: "clear sky"
            DailySummary(day, minTemp, maxTemp, icon, description)
        }
    }
}

fun Forecast.toWeatherResponse(city: City): WeatherResponse {
    return WeatherResponse(
        coord = city.coord,
        weather = this.weather,
        main = this.main,
        wind = this.wind,
        clouds = this.clouds,
        rain = CurrentRain(oneHour = this.rain?.threeHours ?: 0.0),
        dt = this.dt,
        sys = this.sys,
        timezone = city.timezone,
        id = city.id,
        name = city.name,
        base = "",
        visibility = 0,
        cod = 0
    )
}