package com.example.theweatheroracle.nav

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.theweatheroracle.R
import com.example.theweatheroracle.databinding.ActivityNavBinding
import com.example.theweatheroracle.lan.LocaleUtils
import com.example.theweatheroracle.map.MapSelectionDialogFragment
import com.example.theweatheroracle.map.MapViewModel
import com.example.theweatheroracle.map.MapViewModelFactory
import com.example.theweatheroracle.model.weather.WeatherRepositoryImp
import com.example.theweatheroracle.model.api.WeatherRemoteDataSourceImpl
import com.example.theweatheroracle.model.db.weather.WeatherLocalDataSourceImpl
import com.example.theweatheroracle.model.map.MapDataSourceImp
import com.example.theweatheroracle.model.map.MapRetrofitClient
import com.example.theweatheroracle.model.settings.SettingsManager
import com.example.theweatheroracle.settings.Settings
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NavActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityNavBinding
    private lateinit var mapViewModel: MapViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNavBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarNav.toolbar)

        settingsManager = SettingsManager(this)

        mapViewModel = ViewModelProvider(
            this,
            MapViewModelFactory(
                WeatherRepositoryImp.getInstance(
                    WeatherRemoteDataSourceImpl,
                    WeatherLocalDataSourceImpl.getInstance(this)
                ),
                settingsManager,
                MapDataSourceImp
            )
        )[MapViewModel::class.java]

        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        binding.appBarNav.fab.setOnClickListener { view ->
            val mapDialog = MapSelectionDialogFragment { lat, lon ->
                mapViewModel.addCityFromMap(lat, lon)
                lifecycleScope.launch {
                    delay(500)
                    sharedViewModel.triggerRefreshFavorites()
                }
            }
            mapDialog.show(supportFragmentManager, "MapSelectionDialog")
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_nav)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_fav, R.id.nav_alerts
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.nav, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_nav)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, Settings::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        attachBaseContext(LocaleUtils.updateLocale(this))
    }
}