package com.example.theweatheroracle.alerts

import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.theweatheroracle.R
import com.example.theweatheroracle.splash.MainActivity

class AlertReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val weatherMessage = intent.getStringExtra("weather_message") ?: "Weather data unavailable"
        val cityId = intent.getIntExtra("city_id", -1)
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)

        // Try to find the current activity
        val activity = context as? Activity
        if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
            // Show dialog if activity is available
            val navigateIntent = Intent(context, MainActivity::class.java).apply {
                putExtra("city_id", cityId)
                putExtra("latitude", latitude)
                putExtra("longitude", longitude)
                putExtra("navigate_to_home", true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            AlertDialog.Builder(activity)
                .setTitle("Weather Alert")
                .setMessage(weatherMessage)
                .setPositiveButton("View") { _, _ ->
                    activity.startActivity(navigateIntent)
                }
                .setNegativeButton("Dismiss", null)
                .setCancelable(false)
                .show()
        } else {
            showNotification(context, weatherMessage, cityId, latitude, longitude)
        }
    }

    private fun showNotification(context: Context, message: String, cityId: Int, latitude: Double, longitude: Double) {
        val channelId = "weather_alert_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "Weather Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableVibration(true)
            setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null)
        }
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("city_id", cityId)
            putExtra("latitude", latitude)
            putExtra("longitude", longitude)
            putExtra("navigate_to_home", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            cityId, // Using cityId as a unique request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.weatherlogo)
            .setContentTitle("Weather Alert")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .build()

        notificationManager.notify(cityId, notification)
    }
}