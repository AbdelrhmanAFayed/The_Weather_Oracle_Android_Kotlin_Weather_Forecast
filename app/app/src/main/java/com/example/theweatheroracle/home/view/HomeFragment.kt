package com.example.theweatheroracle.home.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.theweatheroracle.databinding.FragmentHomeBinding
import com.example.theweatheroracle.home.viewmodel.HomeViewModel
import com.example.theweatheroracle.home.viewmodel.HomeViewModelFactory
import com.example.theweatheroracle.model.WeatherRepositoryImp
import com.example.theweatheroracle.model.api.WeatherRemoteDataSourceImpl
import com.example.theweatheroracle.model.db.WeatherLocalDataSourceImpl
import com.example.theweatheroracle.model.network.INetworkObserver
import com.example.theweatheroracle.model.network.NetworkObserver
import com.example.theweatheroracle.model.settings.ISettingsManager
import com.example.theweatheroracle.model.settings.SettingsManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var networkObserver: INetworkObserver
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var dailyForecastAdapter: DailyForecastAdapter
    private lateinit var weeklyForecastAdapter: WeeklyForecastAdapter
    private lateinit var settingsManager: ISettingsManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var cityId: Int? = null
    private var isUsingGps: Boolean = false

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchCurrentLocation()
        } else {
            Log.w("HomeFragment", "Location permission denied")
            refreshDataWithoutGps()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsManager = SettingsManager(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val factory = HomeViewModelFactory(
            WeatherRepositoryImp.getInstance(
                WeatherRemoteDataSourceImpl,
                WeatherLocalDataSourceImpl.getInstance(requireContext())
            ),
            settingsManager
        )
        homeViewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        dailyForecastAdapter = DailyForecastAdapter()
        weeklyForecastAdapter = WeeklyForecastAdapter()

        preloadWeatherIcons()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        networkObserver = NetworkObserver(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupRecyclerViews()
        updateLocationData()
        refreshData()
    }

    override fun onResume() {
        super.onResume()
        updateLocationData()
        refreshData()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            networkObserver.observe().collectLatest { status ->
                Log.d("NetworkTest", "Network Status: $status")
                when (status) {
                    INetworkObserver.Status.Available, INetworkObserver.Status.Losing -> {
                        if (isUsingGps) {
                            fetchCurrentLocation()
                        } else {
                            homeViewModel.refreshData(latitude, longitude, cityId, isUsingGps, isOnline())
                        }
                    }
                    INetworkObserver.Status.Unavailable, INetworkObserver.Status.Lost -> {
                        cityId?.let { id ->
                            if (id != 0) {
                                homeViewModel.refreshData(latitude, longitude, cityId, isUsingGps, false)
                            } else {
                                Log.w("HomeFragment", "Invalid cityId (0) for database fetch")
                            }
                        } ?: run {
                            Log.w("HomeFragment", "No cityId available for database fetch")
                        }
                    }
                }
            }
        }

        homeViewModel.city.observe(viewLifecycleOwner) { city ->
            Log.d("HomeFragment", "City observed: $city")
            binding.cityNameText.text = city?.name ?: "Unknown"
        }

        homeViewModel.weather.observe(viewLifecycleOwner) { weather ->
            if (weather != null) {
                val tempUnit = settingsManager.getTemperatureUnit()
                val windUnit = settingsManager.getWindSpeedUnit()

                val (convertedTemp, tempUnitLabel) = convertTemperature(weather.main.temp, tempUnit)
                val (convertedWindSpeed, windUnitLabel) = convertWindSpeed(weather.wind.speed, windUnit)

                binding.weatherDescText.text = weather.weather[0].description
                Glide.with(this)
                    .load("https://openweathermap.org/img/wn/${weather.weather[0].icon}@2x.png")
                    .into(binding.weatherIcon)
                binding.currentTempText.text = String.format("%.1f %s", convertedTemp, tempUnitLabel)
                binding.humidityValueText.text = "${weather.main.humidity}%"
                binding.windSpeedValueText.text = String.format("%.1f %s", convertedWindSpeed, windUnitLabel)
                binding.pressureValueText.text = "${weather.main.pressure} hPa"
                binding.cloudsValueText.text = "${weather.clouds.all}%"
                binding.rainValueText.text = "${weather.rain?.oneHour?.toString() ?: "0"} mm"
            } else {
                binding.weatherDescText.text = "N/A"
                binding.weatherIcon.setImageDrawable(null)
                binding.currentTempText.text = "N/A"
                binding.humidityValueText.text = "N/A"
                binding.windSpeedValueText.text = "N/A"
                binding.pressureValueText.text = "N/A"
                binding.cloudsValueText.text = "N/A"
                binding.rainValueText.text = "N/A"
            }
        }

        homeViewModel.dailyForecasts.observe(viewLifecycleOwner) { forecasts ->
            dailyForecastAdapter.submitList(forecasts)
            dailyForecastAdapter.setTemperatureUnit(settingsManager.getTemperatureUnit())
        }

        homeViewModel.weeklySummaries.observe(viewLifecycleOwner) { summaries ->
            weeklyForecastAdapter.submitList(summaries)
            weeklyForecastAdapter.setTemperatureUnit(settingsManager.getTemperatureUnit())
        }
    }

    private fun setupRecyclerViews() {
        binding.dailyForecastList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.dailyForecastList.adapter = dailyForecastAdapter

        binding.weeklyForecastList.layoutManager = LinearLayoutManager(context)
        binding.weeklyForecastList.adapter = weeklyForecastAdapter
    }

    private fun updateLocationData() {
        isUsingGps = settingsManager.getLocation().lowercase() == "gps"
        if (isUsingGps) {
            settingsManager.setChosenCity("")
            cityId = null
            checkLocationPermission()
        } else {
            latitude = settingsManager.getLatitude() ?: 0.0
            longitude = settingsManager.getLongitude() ?: 0.0
            cityId = settingsManager.getChosenCity().toIntOrNull()
            Log.d("HomeFragment", "Using map mode: cityId=$cityId, lat=$latitude, lon=$longitude")
        }
    }

    private fun refreshData() {
        if (isUsingGps) {
            fetchCurrentLocation()
        } else {
            val isOnline = isOnline()
            if (cityId != null && cityId != 0) {
                homeViewModel.refreshData(latitude, longitude, cityId, isUsingGps, isOnline)
            } else {
                Log.w("HomeFragment", "No cityId available for refresh")
            }
        }

        cityId?.let { id ->
            if (id != 0) {
                homeViewModel.cleanOldForecasts(id)
            }
        }
    }

    private fun refreshDataWithoutGps() {
        latitude = settingsManager.getLatitude() ?: 0.0
        longitude = settingsManager.getLongitude() ?: 0.0
        cityId = settingsManager.getChosenCity().toIntOrNull()
        if (cityId != null && cityId != 0) {
            homeViewModel.refreshData(latitude, longitude, cityId, isUsingGps, isOnline())
        } else {
            Log.w("HomeFragment", "No cityId available for refresh without GPS")
        }
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                fetchCurrentLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Log.w("HomeFragment", "Location permission rationale should be shown")
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            else -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun fetchCurrentLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    Log.d("HomeFragment", "Current location: lat=$latitude, lon=$longitude")
                    settingsManager.setLatitude(latitude)
                    settingsManager.setLongitude(longitude)
                    homeViewModel.refreshData(latitude, longitude, null, isUsingGps, isOnline())
                } else {
                    Log.w("HomeFragment", "Location is null")
                    refreshDataWithoutGps()
                }
            }.addOnFailureListener { e ->
                Log.e("HomeFragment", "Failed to get location: ${e.message}")
                refreshDataWithoutGps()
            }
        } catch (e: SecurityException) {
            Log.e("HomeFragment", "Location permission not granted: ${e.message}")
            refreshDataWithoutGps()
        }
    }

    private fun isOnline(): Boolean {
        val connectivityManager = requireContext().getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        return connectivityManager.activeNetwork != null
    }

    private fun convertTemperature(kelvin: Double, unit: String): Pair<Double, String> {
        return when (unit.lowercase()) {
            "celsius" -> (kelvin - 273.15) to "°C"
            "fahrenheit" -> ((kelvin - 273.15) * 9 / 5 + 32) to "°F"
            else -> kelvin to "K"
        }
    }

    private fun convertWindSpeed(ms: Double, unit: String): Pair<Double, String> {
        return when (unit.lowercase()) {
            "mph" -> (ms * 2.23694) to "mph"
            else -> ms to "m/s"
        }
    }
    private fun preloadWeatherIcons() {
        val baseUrl = "https://openweathermap.org/img/wn/"
        val validIconCodes = listOf("01", "02", "03", "04", "09", "10", "11", "13", "50")
        val suffixes = listOf("d", "n")

        val urls = validIconCodes.flatMap { code ->
            suffixes.map { suffix ->
                "$baseUrl${code}${suffix}@2x.png"
            }
        }

        urls.forEach { url ->
            Glide.with(this)
                .load(url)
                .preload()
        }
    }
}