package com.example.theweatheroracle.lan

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.example.theweatheroracle.model.settings.SettingsManager
import java.util.Locale

object LocaleUtils {
    fun updateLocale(context: Context): Context {
        val settingsManager = SettingsManager(context)
        val language = settingsManager.getLanguage()
        val locale = when (language) {
            "system" -> Locale.getDefault()
            "english" -> Locale.ENGLISH
            "arabic" -> Locale("ar")
            else -> Locale.ENGLISH
        }

        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        return context
    }
}