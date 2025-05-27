package com.example.theweatheroracle.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.theweatheroracle.model.map.IMapDataSource
import com.example.theweatheroracle.model.map.SearchResult
import com.example.theweatheroracle.model.settings.ISettingsManager
import com.example.theweatheroracle.model.weather.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

@Suppress("UNCHECKED_CAST")
class MapViewModelFactory(
    private val repository: WeatherRepository,
    private val settingsManager: ISettingsManager,
    private val mapDataSource: IMapDataSource
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(repository, settingsManager, mapDataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MapViewModel(
    private val repository: WeatherRepository,
    private val settingsManager: ISettingsManager,
    private val mapDataSource: IMapDataSource
) : ViewModel() {

    private val _searchResult = MutableLiveData<SearchResult>()
    val searchResult: LiveData<SearchResult> get() = _searchResult

    private val _selectedLocation = MutableLiveData<GeoPoint?>()
    val selectedLocation: LiveData<GeoPoint?> get() = _selectedLocation

    fun searchCityByName(cityName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = mapDataSource.searchCityByName(cityName)
            result.fold(
                onSuccess = { geoPoint ->
                    _searchResult.postValue(SearchResult.Success(geoPoint))
                },
                onFailure = { exception ->
                    when (exception.message) {
                        "City not found" -> _searchResult.postValue(SearchResult.NotFound)
                        else -> _searchResult.postValue(SearchResult.Error(exception.message ?: "Unknown error"))
                    }
                }
            )
        }
    }

    fun setSelectedLocation(geoPoint: GeoPoint?) {
        _selectedLocation.postValue(geoPoint)
    }

    fun addCityFromMap(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            repository.fetchWeatherForecast(latitude, longitude)
            settingsManager.setLongitude(longitude)
            settingsManager.setLatitude(latitude)
        }
    }

    fun getInitialMapCenter(): GeoPoint {
        val lat = settingsManager.getLatitude() ?: 29.9978
        val lon = settingsManager.getLongitude() ?: 31.0529
        return GeoPoint(lat, lon)
    }
}