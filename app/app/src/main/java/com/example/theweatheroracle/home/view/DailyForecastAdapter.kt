package com.example.theweatheroracle.home.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.theweatheroracle.databinding.DailyForecastItemBinding
import com.example.theweatheroracle.model.Forecast
import com.example.theweatheroracle.model.WeatherDescriptionMapper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class DailyForecastAdapter : ListAdapter<Forecast, DailyForecastAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(val binding: DailyForecastItemBinding) : RecyclerView.ViewHolder(binding.root)

    private var temperatureUnit: String = "Kelvin"
    private var isArabicSelected: Boolean = false

    fun setTemperatureUnit(unit: String) {
        temperatureUnit = unit
    }

    fun setLanguage(isArabic: Boolean) {
        isArabicSelected = isArabic
    }

    fun convertTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(Date(timestamp * 1000))
    }

    private fun convertTemperature(kelvin: Double): Pair<Double, String> {
        return when (temperatureUnit.lowercase()) {
            "celsius" -> (kelvin - 273.15) to "°C"
            "fahrenheit" -> ((kelvin - 273.15) * 9 / 5 + 32) to "°F"
            else -> kelvin to "K"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Forecast>() {
        override fun areItemsTheSame(oldItem: Forecast, newItem: Forecast) =
            oldItem.dt == newItem.dt

        override fun areContentsTheSame(oldItem: Forecast, newItem: Forecast) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DailyForecastItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.apply {
            binding.dayTimeLabel.text = convertTime(item.dt)
            val (convertedTemp, tempUnitLabel) = convertTemperature(item.main.temp)
            binding.dayTempValue.text = String.format("%.1f %s", convertedTemp, tempUnitLabel)

            val description = WeatherDescriptionMapper.getTranslatedDescription(
                item.weather[0].description,
                isArabicSelected
            )
            binding.dayDescText.text = description

            val iconCode = item.weather[0].icon
            val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"
            Glide.with(binding.dayWeatherIcon.context)
                .load(iconUrl)
                .into(binding.dayWeatherIcon)
        }
    }
}