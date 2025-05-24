package com.example.theweatheroracle.home.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.theweatheroracle.databinding.FragmentHomeBinding
import com.example.theweatheroracle.home.viewmodel.HomeViewModel
import com.example.theweatheroracle.home.viewmodel.HomeViewModelFactory
import com.example.theweatheroracle.model.WeatherRepository
import com.example.theweatheroracle.model.WeatherRepositoryImp
import com.example.theweatheroracle.model.api.WeatherRemoteDataSourceImpl
import com.example.theweatheroracle.model.db.WeatherLocalDataSourceImpl
import com.example.theweatheroracle.model.network.INetworkObserver
import com.example.theweatheroracle.model.network.NetworkObserver
import com.example.theweatheroracle.model.settings.ISettingsManager
import com.example.theweatheroracle.model.settings.SettingsManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var networkObserver: INetworkObserver
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var dailyForecastAdapter: DailyForecastAdapter
    private lateinit var weeklyForecastAdapter: WeeklyForecastAdapter
    private lateinit var settingsManager : ISettingsManager
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private  var cityId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsManager = SettingsManager(requireContext())
        latitude = settingsManager.getLatitude() ?: 0.0
        longitude = settingsManager.getLongitude() ?: 0.0
        cityId = settingsManager.getChosenCity().toIntOrNull()


        val factory = HomeViewModelFactory(WeatherRepositoryImp.getInstance(
            WeatherRemoteDataSourceImpl,
            WeatherLocalDataSourceImpl.getInstance(requireContext())
        ))
        homeViewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        dailyForecastAdapter = DailyForecastAdapter()
        weeklyForecastAdapter = WeeklyForecastAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        networkObserver = NetworkObserver(requireContext())
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val connectivityManager = requireContext().getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val isInitiallyOnline = connectivityManager.activeNetwork != null
        if (isInitiallyOnline) {
            homeViewModel.fetchWeatherData(latitude, longitude)
        } else {
            cityId?.let { id ->
                if (id != 0) {
                    homeViewModel.fetchWeatherByCityIdFromDb(id)
                } else {
                    Log.w("HomeFragment", "Invalid cityId (0) for database fetch")
                }
            } ?: run {
                Log.w("HomeFragment", "No cityId available for database fetch")
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            networkObserver.observe().collectLatest { status ->
                Log.d("NetworkTest", "Network Status: $status")
                when (status) {
                    INetworkObserver.Status.Available, INetworkObserver.Status.Losing -> {
                        homeViewModel.fetchWeatherData(latitude, longitude)
                    }
                    INetworkObserver.Status.Unavailable, INetworkObserver.Status.Lost -> {
                        cityId?.let { id ->
                            if (id != 0) {
                                homeViewModel.fetchWeatherByCityIdFromDb(id)
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

        cityId?.let { id ->
            if (id != 0) {
                homeViewModel.cleanOldForecasts(id)
            }
        }

        homeViewModel.city.observe(viewLifecycleOwner) { city ->
            Log.d("HomeFragment", "City: $city")
            binding.cityNameText.text = city?.name ?: "Unknown"
            cityId = city?.id
            settingsManager.setChosenCity(cityId?.toString() ?: "")
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

        binding.dailyForecastList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.dailyForecastList.adapter = dailyForecastAdapter

        binding.weeklyForecastList.layoutManager = LinearLayoutManager(context)
        binding.weeklyForecastList.adapter = weeklyForecastAdapter
    }


    override fun onResume() {
        super.onResume()
            cityId?.let { id ->
                if (id != 0) {
                    homeViewModel.fetchWeatherByCityIdFromDb(id)
                } else {
                    Log.w("HomeFragment", "Invalid cityId (0) for database fetch")
                }
            } ?: run {
                Log.w("HomeFragment", "No cityId available for database fetch")
            }

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
}