package com.example.theweatheroracle.fav

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.theweatheroracle.model.weather.City
import com.example.theweatheroracle.model.weather.WeatherRepository
import com.example.theweatheroracle.model.settings.ISettingsManager
import kotlinx.coroutines.launch


data class CityWithTemperature( val city: City, val latestTemperature: Double? )

    @Suppress("UNCHECKED_CAST")
class FavouritesViewModelFactory(
    private val repository: WeatherRepository,
    private val settingsManager: ISettingsManager )
        : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(FavouritesViewModel::class.java)) {
        return FavouritesViewModel(repository, settingsManager) as T
    }
        throw IllegalArgumentException("Unknown ViewModel class") }
    }


class FavouritesViewModel(
    private val repository: WeatherRepository,
    private val settingsManager: ISettingsManager )
    : ViewModel() {
    private val _favoriteCities = MutableLiveData<List<CityWithTemperature>>(emptyList())
    val favoriteCities: LiveData<List<CityWithTemperature>> = _favoriteCities

    init {
        fetchFavoriteCities()
    }

    fun fetchFavoriteCities() {
        viewModelScope.launch {
            val cities = repository.getAllCities()
            val citiesWithTemps = cities.mapNotNull { city ->
                val forecasts = repository.getForecastsForCity(city.id)
                val latestForecast = forecasts.minByOrNull { it.dt }
                if (latestForecast != null) {
                    CityWithTemperature(city, latestForecast.main.temp)
                } else {
                    CityWithTemperature(city, null)
                }
            }
            _favoriteCities.postValue(citiesWithTemps)
        }
    }

    fun removeCityFromFavorites(cityId: Int) {
        viewModelScope.launch {
            repository.deleteCityById(cityId)
            fetchFavoriteCities()
        }
    }

    fun setCurrentCity(cityId: Int) {
        settingsManager.setChosenCity(cityId.toString())
        if (settingsManager.getLocation().lowercase() == "gps") {
            settingsManager.setLocation("map")
        }
    }


}