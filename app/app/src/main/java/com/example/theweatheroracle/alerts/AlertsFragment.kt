package com.example.theweatheroracle.alerts

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.theweatheroracle.databinding.FragmentAlertsBinding
import com.example.theweatheroracle.model.api.WeatherRemoteDataSourceImpl
import com.example.theweatheroracle.model.db.AppDatabase
import com.example.theweatheroracle.model.db.weather.WeatherLocalDataSourceImpl
import com.example.theweatheroracle.model.settings.SettingsManager
import com.example.theweatheroracle.model.weather.City
import com.example.theweatheroracle.model.weather.WeatherRepositoryImp
import kotlinx.coroutines.launch

class AlertsFragment : Fragment() {
    private lateinit var binding: FragmentAlertsBinding
    private lateinit var alertViewModel: AlertViewModel
    private lateinit var alertAdapter: AlertAdapter
    private var permissionsGranted = false

    private val requestNotificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        checkAllPermissions()
    }

    private val requestOverlayPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        checkAllPermissions()
    }

    private val requestBatteryOptimization = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        checkAllPermissions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()
        val factory = AlertViewModelFactory(
            WeatherRepositoryImp.getInstance(
                WeatherRemoteDataSourceImpl,
                WeatherLocalDataSourceImpl.getInstance(requireContext())
            ),
            SettingsManager(requireContext()),
            AppDatabase.getDatabase(requireContext())
        )
        alertViewModel = ViewModelProvider(this, factory)[AlertViewModel::class.java]
        alertAdapter = AlertAdapter { alert ->
            alertViewModel.deleteAlert(alert)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupAddAlertButton()
        setupObservers()
    }

    private fun setupRecyclerView() {
        binding.alertsList.layoutManager = LinearLayoutManager(context)
        binding.alertsList.adapter = alertAdapter
    }

    private fun setupAddAlertButton() {
        binding.addAlertButton.setOnClickListener {
            if (!permissionsGranted) {
                Toast.makeText(context, "Please grant all permissions to add alerts", Toast.LENGTH_SHORT).show()
                requestPermissions()
                return@setOnClickListener
            }
            viewLifecycleOwner.lifecycleScope.launch {
                val favorites = AppDatabase.getDatabase(requireContext()).cityDao().getAllCities()
                val cityList = favorites.map { City(
                    it.id, it.name, it.coord,
                    country = it.country,
                    population = it.population,
                    timezone = it.timezone,
                    sunrise = it.sunrise,
                    sunset = it.sunset
                ) }
                AddAlertDialogFragment(cityList) { alert ->
                    alertViewModel.addAlert(alert)
                }.show(childFragmentManager, "AddAlertDialog")
            }
        }
    }

    private fun setupObservers() {
        alertViewModel.alerts.observe(viewLifecycleOwner) { alerts ->
            alertAdapter.submitList(alerts)
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        if ( !Settings.canDrawOverlays(requireContext())) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:${requireContext().packageName}"))
            requestOverlayPermission.launch(intent)
        }

        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        intent.data = android.net.Uri.parse("package:${requireContext().packageName}")
        requestBatteryOptimization.launch(intent)
    }

    private fun checkAllPermissions() {
        val notificationGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val overlayGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(requireContext())
        val batteryOptOut = true // Cannot programmatically check this; assume granted after user action

        permissionsGranted = notificationGranted && overlayGranted && batteryOptOut
        if (!permissionsGranted) {
            Toast.makeText(context, "Some permissions are missing. Please grant all to use alerts.", Toast.LENGTH_LONG).show()
            requestPermissions()
        } else {
            Toast.makeText(context, "Permissions granted. You can now use alerts.", Toast.LENGTH_SHORT).show()
        }
    }
}