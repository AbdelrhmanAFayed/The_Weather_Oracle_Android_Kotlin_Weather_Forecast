package com.example.theweatheroracle.alerts

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.theweatheroracle.R
import com.example.theweatheroracle.model.api.WeatherRemoteDataSourceImpl
import com.example.theweatheroracle.model.db.weather.WeatherLocalDataSourceImpl
import com.example.theweatheroracle.model.settings.SettingsManager
import com.example.theweatheroracle.model.weather.WeatherRepositoryImp
import com.example.theweatheroracle.splash.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AlertWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    private val alertId: Int = inputData.getInt("alert_id", -1)

    override suspend fun doWork(): Result {
        Log.d("AlertWorker", "Work started for alertId: $alertId")
        val cityId = inputData.getInt("city_id", -1)
        val latitude = inputData.getDouble("latitude", 0.0)
        val longitude = inputData.getDouble("longitude", 0.0)
        val type = inputData.getString("type") ?: "notification"

        val repository = WeatherRepositoryImp.getInstance(
            WeatherRemoteDataSourceImpl,
            WeatherLocalDataSourceImpl.getInstance(applicationContext)
        )
        val settingsManager = SettingsManager(applicationContext)

        val weatherResult = withContext(Dispatchers.IO) {
            if (cityId != -1) {
                repository.fetchWeatherByCityId(
                    cityId,
                    settingsManager.getTemperatureUnit(),
                    null,
                    settingsManager.getLanguage()
                )
            } else {
                repository.fetchWeatherByLatLon(
                    latitude,
                    longitude,
                    settingsManager.getTemperatureUnit(),
                    null,
                    settingsManager.getLanguage()
                )
            }
        }
        val weatherMessage = weatherResult.getOrNull()?.let { weather ->
            val tempUnit = settingsManager.getTemperatureUnit()
            val (convertedTemp, tempUnitLabel) = convertTemperature(weather.main.temp, tempUnit)
            "Temperature: ${String.format("%.1f %s", convertedTemp, tempUnitLabel)}"
        } ?: "Weather data unavailable"

        when (type) {
            "notification" -> showNotification(weatherMessage, cityId, latitude, longitude)
            "popup" -> startAlertService(weatherMessage, cityId, latitude, longitude)
        }

        return Result.success()
    }

    private fun showNotification(message: String, cityId: Int, latitude: Double, longitude: Double) {
        Log.d("AlertWorker", "Showing notification for alertId: $alertId, cityId: $cityId")
        val channelId = "weather_alert_channel"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "Weather Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableVibration(true)
            setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null)
        }
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            putExtra("city_id", cityId)
            putExtra("latitude", latitude)
            putExtra("longitude", longitude)
            putExtra("navigate_to_home", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            alertId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.weatherlogo)
            .setContentTitle("Weather Alert")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .build()

        notificationManager.notify(alertId, notification)
    }

    private fun startAlertService(message: String, cityId: Int, latitude: Double, longitude: Double) {
        Log.d("AlertWorker", "Starting AlertService for alertId: $alertId, cityId: $cityId")
        val intent = Intent(applicationContext, AlertService::class.java).apply {
            putExtra("weather_message", message)
            putExtra("city_id", cityId)
            putExtra("latitude", latitude)
            putExtra("longitude", longitude)
        }
        applicationContext.startForegroundService(intent)
    }

    private fun convertTemperature(kelvin: Double, unit: String): Pair<Double, String> {
        return when (unit.lowercase()) {
            "celsius" -> (kelvin - 273.15) to "°C"
            "fahrenheit" -> ((kelvin - 273.15) * 9 / 5 + 32) to "°F"
            else -> kelvin to "K"
        }
    }
}