package com.example.theweatheroracle.viewmodel

import android.R.attr.timeZone
import com.example.theweatheroracle.map.MapViewModel
import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.theweatheroracle.home.viewmodel.HomeViewModel
import com.example.theweatheroracle.model.location.LocationDataSource
import com.example.theweatheroracle.model.settings.ISettingsManager
import com.example.theweatheroracle.model.weather.WeatherRepository
import com.example.theweatheroracle.model.map.IMapDataSource
import com.example.theweatheroracle.model.network.INetworkObserver
import com.example.theweatheroracle.model.weather.City
import com.example.theweatheroracle.model.weather.WeatherResponse
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.util.GeoPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@RunWith(AndroidJUnit4::class)
class HomeViewModelTest {

    private lateinit var repository: WeatherRepository
    private lateinit var settingsManager: ISettingsManager
    private lateinit var locationDataSource: LocationDataSource
    private lateinit var networkObserver: INetworkObserver
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        settingsManager = mockk(relaxed = true)
        locationDataSource = mockk(relaxed = true)
        networkObserver = mockk(relaxed = true)
        viewModel = HomeViewModel(repository, settingsManager, locationDataSource, networkObserver)
    }

    @Test
    fun cleanOldForecasts_deletesForecastsBeforeCurrentTime() = runBlocking {
        // Given: A city ID
        val cityId = 123

        // When: cleanOldForecasts is called
        viewModel.cleanOldForecasts(cityId)

        // Then: Repository deletes forecasts before the current timestamp
        coVerify(exactly = 1) {
            repository.deleteForecastsForCityBeforeDt(cityId, any())
        }
    }

    @Test
    fun refreshData_offlineNoGpsNoCityId_clearsWeatherData() = runBlocking {
        // Given: No GPS, no city ID, offline mode
        val latitude = 40.7128
        val longitude = -74.0060
        val useGps = false
        val cityId: Int? = null
        val isOnline = false
        var observedWeather: WeatherResponse? = null
        val weatherObserver = Observer<WeatherResponse?> { value ->
            observedWeather = value
            println("Observed weather: $value")
        }
        var observedCity: City? = null
        val cityObserver = Observer<City?> { value ->
            observedCity = value
            println("Observed city: $value")
        }

        // When: refreshData is called
        viewModel.weather.observeForever(weatherObserver)
        viewModel.city.observeForever(cityObserver)
        viewModel.refreshData(latitude, longitude, cityId, useGps, isOnline)
        viewModel.weather.removeObserver(weatherObserver)
        viewModel.city.removeObserver(cityObserver)

        // Then: Weather and city LiveData are set to null
        assertNull(observedWeather)
        assertNull(observedCity)
    }
}