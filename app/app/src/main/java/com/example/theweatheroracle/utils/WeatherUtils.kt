package com.example.theweatheroracle.utils

object WeatherUtils {
    fun convertTemperature(kelvin: Double, unit: String): Pair<Double, String> {
        return when (unit.lowercase()) {
            "celsius" -> (kelvin - 273.15) to "°C"
            "fahrenheit" -> ((kelvin - 273.15) * 9 / 5 + 32) to "°F"
            else -> kelvin to "K"
        }
    }

    fun convertWindSpeed(ms: Double, unit: String): Pair<Double, String> {
        return when (unit.lowercase()) {
            "mph" -> (ms * 2.23694) to "mph"
            else -> ms to "m/s"
        }
    }

    fun isArabicSelected(language: String): Boolean {
        return language == "arabic" || (language == "system" && java.util.Locale.getDefault().language == "ar")
    }
}