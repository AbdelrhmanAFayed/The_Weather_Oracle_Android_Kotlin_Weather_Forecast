package com.example.theweatheroracle.home.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.theweatheroracle.R
import com.example.theweatheroracle.databinding.FragmentHomeBinding
import com.example.theweatheroracle.home.viewmodel.HomeViewModel
import com.example.theweatheroracle.home.viewmodel.HomeViewModelFactory
import com.example.theweatheroracle.map.MapSelectionDialogFragment
import com.example.theweatheroracle.model.weather.WeatherDescriptionMapper
import com.example.theweatheroracle.model.weather.WeatherRepositoryImp
import com.example.theweatheroracle.model.api.WeatherRemoteDataSourceImpl
import com.example.theweatheroracle.model.db.weather.WeatherLocalDataSourceImpl
import com.example.theweatheroracle.model.location.FusedLocationDataSource
import com.example.theweatheroracle.model.network.INetworkObserver
import com.example.theweatheroracle.model.network.NetworkObserver
import com.example.theweatheroracle.model.settings.ISettingsManager
import com.example.theweatheroracle.model.settings.SettingsManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var networkObserver: INetworkObserver
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var dailyForecastAdapter: DailyForecastAdapter
    private lateinit var weeklyForecastAdapter: WeeklyForecastAdapter
    private lateinit var settingsManager: ISettingsManager
    private lateinit var locationDataSource: FusedLocationDataSource
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var cityId: Int? = null
    private var isUsingGps: Boolean = false
    private var isDialogShowing: Boolean = false

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchCurrentLocation()
        } else {
            Log.w("HomeFragment", "Location permission denied")
            if (!isDialogShowing) {
                showPermissionDeniedDialog()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsManager = SettingsManager(requireContext())
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationDataSource = FusedLocationDataSource(requireContext(), fusedLocationClient)

        val factory = HomeViewModelFactory(
            WeatherRepositoryImp.getInstance(
                WeatherRemoteDataSourceImpl,
                WeatherLocalDataSourceImpl.getInstance(requireContext())
            ),
            settingsManager,
            locationDataSource,
            NetworkObserver(requireContext())
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
        setupRefreshButton()
        updateLocationData()
        refreshData()
    }

    override fun onResume() {
        super.onResume()
        updateLocationData()
        refreshData()
        updateLanguageSettings()
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
                            homeViewModel.refreshData(latitude, longitude, cityId, isUsingGps, networkObserver.isOnline())
                        }
                    }
                    INetworkObserver.Status.Unavailable, INetworkObserver.Status.Lost -> {
                        val hasWeatherData = homeViewModel.weather.value != null
                        val hasDailyData = homeViewModel.dailyForecasts.value?.isNotEmpty() == true
                        val hasWeeklyData = homeViewModel.weeklySummaries.value?.isNotEmpty() == true

                        if (hasWeatherData || hasDailyData || hasWeeklyData) {
                            Log.d("HomeFragment", "Keeping existing data as offline with data present")
                        } else {
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
        }

        homeViewModel.city.observe(viewLifecycleOwner) { city ->
            Log.d("HomeFragment", "City observed: $city")
            binding.cityNameText.text = city?.name ?: getString(R.string.unknown)
        }

        homeViewModel.weather.observe(viewLifecycleOwner) { weather ->
            if (weather != null) {
                val tempUnit = settingsManager.getTemperatureUnit()
                val windUnit = settingsManager.getWindSpeedUnit()
                val isArabicSelected = settingsManager.getLanguage().let { language ->
                    language == "arabic" || (language == "system" && Locale.getDefault().language == "ar")
                }

                val (convertedTemp, tempUnitLabel) = convertTemperature(weather.main.temp, tempUnit)
                val (convertedWindSpeed, windUnitLabel) = convertWindSpeed(weather.wind.speed, windUnit)

                val description = WeatherDescriptionMapper.getTranslatedDescription(
                    weather.weather[0].description,
                    isArabicSelected
                )
                binding.weatherDescText.text = description

                Glide.with(this@HomeFragment)
                    .load("https://openweathermap.org/img/wn/${weather.weather[0].icon}@2x.png")
                    .into(binding.weatherIcon)
                binding.currentTempText.text = String.format("%.1f %s", convertedTemp, tempUnitLabel)
                binding.humidityValueText.text = "${weather.main.humidity}%"
                binding.windSpeedValueText.text = String.format("%.1f %s", convertedWindSpeed, windUnitLabel)
                binding.pressureValueText.text = "${weather.main.pressure} hPa"
                binding.cloudsValueText.text = "${weather.clouds.all}%"
                binding.rainValueText.text = "${weather.rain?.oneHour?.toString() ?: "0"} mm"
            }
        }

        homeViewModel.dailyForecasts.observe(viewLifecycleOwner) { forecasts ->
            dailyForecastAdapter.submitList(forecasts.take(8))
            dailyForecastAdapter.setTemperatureUnit(settingsManager.getTemperatureUnit())
            updateLanguageSettings()
        }

        homeViewModel.weeklySummaries.observe(viewLifecycleOwner) { summaries ->
            weeklyForecastAdapter.submitList(summaries)
            weeklyForecastAdapter.setTemperatureUnit(settingsManager.getTemperatureUnit())
            updateLanguageSettings()
        }

        homeViewModel.lastUpdated.observe(viewLifecycleOwner) { timestamp ->
            binding.lastUpdatedText.text = timestamp?.let { getString(R.string.data_shown_for_na, it) } ?: getString(R.string.last_updated_na)
        }
    }

    private fun setupRecyclerViews() {
        binding.dailyForecastList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.dailyForecastList.adapter = dailyForecastAdapter

        binding.weeklyForecastList.layoutManager = LinearLayoutManager(context)
        binding.weeklyForecastList.adapter = weeklyForecastAdapter
    }

    private fun setupRefreshButton() {
        binding.refreshButton.setOnClickListener {
            refreshData()
        }
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
            homeViewModel.refreshData(latitude, longitude, cityId, isUsingGps, networkObserver.isOnline())
        }
        dailyForecastAdapter.setTemperatureUnit(settingsManager.getTemperatureUnit())
        weeklyForecastAdapter.setTemperatureUnit(settingsManager.getTemperatureUnit())
        updateLanguageSettings()

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
        homeViewModel.refreshData(latitude, longitude, cityId, isUsingGps, networkObserver.isOnline())
    }

    private fun checkLocationPermission() {
        if (isDialogShowing) return
        when {
            locationDataSource.hasLocationPermission() -> {
                fetchCurrentLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showPermissionDeniedDialog()
            }
            else -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun fetchCurrentLocation() {
        if (isDialogShowing) return
        if (!locationDataSource.isLocationServiceEnabled()) {
            showLocationServicesDisabledDialog()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val location = locationDataSource.getLastKnownLocation()
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    Log.d("HomeFragment", "Current location: lat=$latitude, lon=$longitude")
                    settingsManager.setLatitude(latitude)
                    settingsManager.setLongitude(longitude)
                    homeViewModel.refreshData(latitude, longitude, null, isUsingGps, networkObserver.isOnline())
                } else {
                    Log.w("HomeFragment", "Location is null")
                    if (!isDialogShowing) {
                        showPermissionDeniedDialog()
                    }
                }
            } catch (e: SecurityException) {
                Log.e("HomeFragment", "Location permission not granted: ${e.message}")
                if (!isDialogShowing) {
                    showPermissionDeniedDialog()
                }
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        isDialogShowing = true
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.select_location)
            .setMessage(R.string.location_permission_denied)
            .setPositiveButton(R.string.grant_permissions) { _, _ ->
                isDialogShowing = false
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            .setNegativeButton(R.string.use_map) { _, _ ->
                isDialogShowing = false
                switchToMapMode()
            }
            .setCancelable(false)
            .setOnDismissListener { isDialogShowing = false }
            .show()
    }

    private fun showLocationServicesDisabledDialog() {
        isDialogShowing = true
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.select_location)
            .setMessage(R.string.location_services_disabled)
            .setPositiveButton(R.string.enable_location) { _, _ ->
                isDialogShowing = false
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton(R.string.use_map) { _, _ ->
                isDialogShowing = false
                switchToMapMode()
            }
            .setCancelable(false)
            .setOnDismissListener { isDialogShowing = false }
            .show()
    }

    private fun switchToMapMode() {
        settingsManager.setLocation("map")
        settingsManager.setChosenCity("")
        isUsingGps = false
        cityId = null
        Log.d("HomeFragment", "Switching to map mode")
        val mapDialog = MapSelectionDialogFragment { lat, lon ->
            latitude = lat
            longitude = lon
            settingsManager.setLatitude(lat)
            settingsManager.setLongitude(lon)
            Log.d("HomeFragment", "Map selected: lat=$lat, lon=$lon")
            refreshData()
        }
        mapDialog.show(parentFragmentManager, "MapSelectionDialog")
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

    private fun updateLanguageSettings() {
        val isArabicSelected = settingsManager.getLanguage().let { language ->
            language == "arabic" || (language == "system" && Locale.getDefault().language == "ar")
        }
        dailyForecastAdapter.setLanguage(isArabicSelected)
        weeklyForecastAdapter.setLanguage(isArabicSelected)
    }
}