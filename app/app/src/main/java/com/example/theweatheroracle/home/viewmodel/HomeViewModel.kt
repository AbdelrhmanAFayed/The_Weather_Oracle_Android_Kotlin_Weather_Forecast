package com.example.theweatheroracle.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.theweatheroracle.home.view.DailySummary
import com.example.theweatheroracle.model.Forecast
import com.example.theweatheroracle.model.WeatherForecastResponse
import com.example.theweatheroracle.model.WeatherRepository
import com.example.theweatheroracle.model.City
import com.example.theweatheroracle.model.WeatherResponse
import com.example.theweatheroracle.model.settings.ISettingsManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class HomeViewModelFactory(
    private val repository: WeatherRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class HomeViewModel(
    private val repository: WeatherRepository
) : ViewModel() {
    private val _city = MutableLiveData<City?>()
    val city: LiveData<City?> = _city

    private val _weather = MutableLiveData<WeatherResponse?>()
    val weather: LiveData<WeatherResponse?> = _weather

    private val _dailyForecasts = MutableLiveData<List<Forecast>>()
    val dailyForecasts: LiveData<List<Forecast>> = _dailyForecasts

    private val _weeklySummaries = MutableLiveData<List<DailySummary>>()
    val weeklySummaries: LiveData<List<DailySummary>> = _weeklySummaries

    fun fetchWeatherData(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val forecastResult = repository.fetchWeatherForecast(latitude, longitude, "standard", null, null, null)
            forecastResult.onSuccess { forecastResponse ->
                _city.postValue(forecastResponse.city)
                _dailyForecasts.postValue(forecastResponse.list)
                _weeklySummaries.postValue(computeWeeklySummaries(forecastResponse.list))
            }
            val weatherResult = repository.fetchWeatherByLatLon(latitude, longitude, "standard", null, null)
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
            }
        }
    }

     fun fetchWeatherByCityIdFromDb(cityId: Int) {
         viewModelScope.launch {
             val city = repository.getCityById(cityId)
             if (city != null) {
                 _city.postValue(city) // Ensure city is emitted
                 val forecasts = repository.getForecastsForCity(cityId)
                 _dailyForecasts.postValue(forecasts)
                 _weeklySummaries.postValue(computeWeeklySummaries(forecasts))
             } else {
                 _city.postValue(null)
                 _dailyForecasts.postValue(emptyList())
                 _weeklySummaries.postValue(emptyList())
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
            DailySummary(day, minTemp, maxTemp, icon)
        }
    }
}