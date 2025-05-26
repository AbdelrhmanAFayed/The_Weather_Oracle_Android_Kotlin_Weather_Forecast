package com.example.theweatheroracle.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.theweatheroracle.model.weather.WeatherRepository
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
class MapViewModelFactory(private val repository: WeatherRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MapViewModel(private val repository: WeatherRepository) : ViewModel() {

    fun addCityFromMap(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val result  = repository.fetchWeatherForecast(latitude,longitude)

        }
    }
}