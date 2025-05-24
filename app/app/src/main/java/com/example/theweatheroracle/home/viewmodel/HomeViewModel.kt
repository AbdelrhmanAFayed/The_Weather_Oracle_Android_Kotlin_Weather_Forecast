package com.example.theweatheroracle.home.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.theweatheroracle.home.view.DailySummary
import com.example.theweatheroracle.model.City
import com.example.theweatheroracle.model.Coord
import com.example.theweatheroracle.model.CurrentRain
import com.example.theweatheroracle.model.Forecast
import com.example.theweatheroracle.model.Sys
import com.example.theweatheroracle.model.WeatherRepository
import com.example.theweatheroracle.model.WeatherResponse
import com.example.theweatheroracle.model.settings.ISettingsManager
import com.example.theweatheroracle.settings.SettingsViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale


@Suppress("UNCHECKED_CAST")
class HomeViewModelFactory(private val repository: WeatherRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException()
    }
}


class HomeViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _city = MutableLiveData<City?>(null)
    val city: LiveData<City?> = _city

    private val _dailyForecasts = MutableLiveData<List<Forecast>>(emptyList())
    val dailyForecasts: LiveData<List<Forecast>> = _dailyForecasts

    private val _weeklySummaries = MutableLiveData<List<DailySummary>>(emptyList())
    val weeklySummaries: LiveData<List<DailySummary>> = _weeklySummaries

    private val _weather = MutableLiveData<WeatherResponse?>(null)
    val weather: LiveData<WeatherResponse?> = _weather

    fun fetchWeatherData(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val weatherResult = repository.fetchWeatherByLatLon(latitude, longitude)
            weatherResult.onSuccess { weatherResponse ->
                Log.d("HomeViewModel", "WeatherResponse: $weatherResponse")
                _weather.postValue(weatherResponse)
                // Note: No direct saveWeather method; assuming repository caches internally
                val forecastResult = repository.fetchWeatherForecast(latitude, longitude, cnt = 40)
                forecastResult.onSuccess { forecastResponse ->
                    Log.d("HomeViewModel", "ForecastResponse City: ${forecastResponse.city}")
                    _city.postValue(forecastResponse.city)
                    _dailyForecasts.postValue(forecastResponse.list)
                    // Note: No direct saveForecasts method; assuming repository caches internally

                    val summaries = forecastResponse.list.groupBy { forecast ->
                        val calendar = Calendar.getInstance().apply { timeInMillis = forecast.dt * 1000 }
                        calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
                    }.mapNotNull { (day, forecasts) ->
                        forecasts.minByOrNull { it.dt }?.let { minForecast ->
                            DailySummary(
                                day = day,
                                minTemp = forecasts.minOf { it.main.temp },
                                maxTemp = "${forecasts.maxOf { it.main.temp }}K",
                                icon = minForecast.weather[0].icon
                            )
                        }
                    }.take(5)
                    _weeklySummaries.postValue(summaries)
                }.onFailure { throwable ->
                    Log.e("HomeViewModel", "Forecast fetch failed: ${throwable.message}")
                }
            }.onFailure { throwable ->
                Log.e("HomeViewModel", "Weather fetch failed: ${throwable.message}")
            }
        }
    }

    fun fetchWeatherByCityId(cityId: Int) {
        viewModelScope.launch {
            val weatherResult = repository.fetchWeatherByCityId(cityId)
            weatherResult.onSuccess { weatherResponse ->
                Log.d("HomeViewModel", "WeatherResponse by City ID: $weatherResponse")
                _weather.postValue(weatherResponse)
                val forecastResult = repository.fetchForecastByCityId(cityId, cnt = 40)
                forecastResult.onSuccess { forecastResponse ->
                    Log.d("HomeViewModel", "ForecastResponse by City ID: ${forecastResponse.city}")
                    _city.postValue(forecastResponse.city)
                    _dailyForecasts.postValue(forecastResponse.list)

                    val summaries = forecastResponse.list.groupBy { forecast ->
                        val calendar = Calendar.getInstance().apply { timeInMillis = forecast.dt * 1000 }
                        calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
                    }.mapNotNull { (day, forecasts) ->
                        forecasts.minByOrNull { it.dt }?.let { minForecast ->
                            DailySummary(
                                day = day,
                                minTemp = forecasts.minOf { it.main.temp },
                                maxTemp = "${forecasts.maxOf { it.main.temp }}K",
                                icon = minForecast.weather[0].icon
                            )
                        }
                    }.take(5)
                    _weeklySummaries.postValue(summaries)
                }.onFailure { throwable ->
                    Log.e("HomeViewModel", "Forecast fetch by City ID failed: ${throwable.message}")
                }
            }.onFailure { throwable ->
                Log.e("HomeViewModel", "Weather fetch by City ID failed: ${throwable.message}")
            }
        }
    }

    fun fetchWeatherByCityIdFromDb(cityId: Int) {
        viewModelScope.launch {
            val cachedCity = repository.getCityById(cityId)
            val cachedForecasts = repository.getForecastsForCity(cityId)

            if (cachedCity != null) {
                _city.postValue(cachedCity)
                Log.d("HomeViewModel", "Fetched City from DB: $cachedCity")
            }

            if (cachedForecasts.isNotEmpty()) {
                val latestForecast = cachedForecasts.minByOrNull { it.dt }
                if (latestForecast != null) {
                    val weatherResponse = WeatherResponse(
                        coord = cachedCity?.coord ?: Coord(0.0, 0.0),
                        weather = latestForecast.weather,
                        main = latestForecast.main,
                        wind = latestForecast.wind,
                        clouds = latestForecast.clouds,
                        rain = CurrentRain(oneHour = latestForecast.rain?.threeHours ?: 0.0),
                        dt = latestForecast.dt,
                        sys = Sys(), // Not available in Forecast
                        name = cachedCity?.name ?: "Unknown",
                        id = cityId,
                        base = "",
                        visibility = 0,
                        timezone = 0,
                        cod = 0
                    )
                    _weather.postValue(weatherResponse)
                    Log.d("HomeViewModel", "Approximated WeatherResponse from DB: $weatherResponse")
                }

                _dailyForecasts.postValue(cachedForecasts)
                Log.d("HomeViewModel", "Fetched Forecasts from DB: $cachedForecasts")

                val summaries = cachedForecasts.groupBy { forecast ->
                    val calendar = Calendar.getInstance().apply { timeInMillis = forecast.dt * 1000 }
                    calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
                }.mapNotNull { (day, forecasts) ->
                    forecasts.minByOrNull { it.dt }?.let { minForecast ->
                        DailySummary(
                            day = day,
                            minTemp = forecasts.minOf { it.main.temp },
                            maxTemp = "${forecasts.maxOf { it.main.temp }}K",
                            icon = minForecast.weather[0].icon
                        )
                    }
                }.take(5)
                _weeklySummaries.postValue(summaries)
            } else {
                Log.w("HomeViewModel", "No cached data found for cityId: $cityId")
            }
        }
    }
    fun cleanOldForecasts(cityId: Int) {
        viewModelScope.launch {
            val currentDt = System.currentTimeMillis() / 1000
            repository.deleteForecastsForCityBeforeDt(cityId, currentDt)
            Log.d("HomeViewModel", "Deleted forecasts before dt: $currentDt for cityId: $cityId")
        }
    }
}