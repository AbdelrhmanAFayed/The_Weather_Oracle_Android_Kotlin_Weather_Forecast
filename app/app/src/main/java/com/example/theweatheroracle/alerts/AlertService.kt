package com.example.theweatheroracle.alerts

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.theweatheroracle.R
import com.example.theweatheroracle.splash.MainActivity
import android.util.Log

class AlertService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val weatherMessage = intent?.getStringExtra("weather_message") ?: "Weather data unavailable"
        val cityId = intent?.getIntExtra("city_id", -1)
        latitude = intent?.getDoubleExtra("latitude", 0.0) ?: 0.0
        longitude = intent?.getDoubleExtra("longitude", 0.0) ?: 0.0

        startForeground(NOTIFICATION_ID, createNotification(weatherMessage, cityId ?: -1))
        showPopupOrFallback(weatherMessage, cityId, latitude, longitude)

        return START_NOT_STICKY
    }

    private fun createNotification(message: String, cityId: Int): android.app.Notification {
        val channelId = "alert_service_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alert Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("city_id", cityId)
            putExtra("latitude", latitude)
            putExtra("longitude", longitude)
            putExtra("navigate_to_home", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            cityId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.weatherlogo)
            .setContentTitle("Weather Alert Active")
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    private fun showPopupOrFallback(message: String, cityId: Int?, latitude: Double, longitude: Double) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Log.e("AlertService", "SYSTEM_ALERT_WINDOW permission denied, showing fallback notification")
            showFallbackNotification(message, cityId ?: -1, latitude, longitude)
            stopSelf()
            return
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        floatingView = LayoutInflater.from(this).inflate(R.layout.popup_alert, null)

        val messageText = floatingView?.findViewById<TextView>(R.id.popupMessage)
        val viewButton = floatingView?.findViewById<Button>(R.id.popupViewButton)
        val dismissButton = floatingView?.findViewById<Button>(R.id.popupDismissButton)

        messageText?.text = message

        viewButton?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("city_id", cityId)
                putExtra("latitude", latitude)
                putExtra("longitude", longitude)
                putExtra("navigate_to_home", true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
            stopSelf()
        }

        dismissButton?.setOnClickListener {
            stopSelf()
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            android.graphics.PixelFormat.TRANSLUCENT
        ).apply {
            gravity = android.view.Gravity.CENTER
        }

        try {
            windowManager?.addView(floatingView, params)
        } catch (e: WindowManager.BadTokenException) {
            Log.e("AlertService", "Failed to add overlay: ${e.message}")
            showFallbackNotification(message, cityId ?: -1, latitude, longitude)
            stopSelf()
        }
    }

    private fun showFallbackNotification(message: String, cityId: Int, latitude: Double, longitude: Double) {
        val channelId = "weather_alert_fallback_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Weather Alert Fallback",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("city_id", cityId)
            putExtra("latitude", latitude)
            putExtra("longitude", longitude)
            putExtra("navigate_to_home", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            cityId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
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

    override fun onDestroy() {
        super.onDestroy()
        windowManager?.removeView(floatingView)
        stopForeground(true)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 1001
    }
}