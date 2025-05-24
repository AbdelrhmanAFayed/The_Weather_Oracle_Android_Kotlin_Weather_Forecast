package com.example.theweatheroracle.home.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.theweatheroracle.databinding.WeeklyForecastItemBinding

data class DailySummary(
    val day: String,
    val minTemp: Double,
    val maxTemp: String,
    val icon: String
)

class WeeklyForecastAdapter : ListAdapter<DailySummary, WeeklyForecastAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder( val binding: WeeklyForecastItemBinding) : RecyclerView.ViewHolder(binding.root)

    class DiffCallback : DiffUtil.ItemCallback<DailySummary>() {
        override fun areItemsTheSame(oldItem: DailySummary, newItem: DailySummary) =
            oldItem.day == newItem.day

        override fun areContentsTheSame(oldItem: DailySummary, newItem: DailySummary) =
            oldItem == newItem
    }

    private var temperatureUnit: String = "Kelvin"

    fun setTemperatureUnit(unit: String) {
        temperatureUnit = unit
    }

    private fun convertTemperature(kelvin: Double): Pair<Double, String> {
        return when (temperatureUnit.lowercase()) {
            "celsius" -> (kelvin - 273.15) to "°C"
            "fahrenheit" -> ((kelvin - 273.15) * 9 / 5 + 32) to "°F"
            else -> kelvin to "K"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = WeeklyForecastItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.apply {
            binding.weekDayLabel.text = item.day
            val (convertedMinTemp, tempUnitLabel) = convertTemperature(item.minTemp)
            val maxTempValue = item.maxTemp.replace("K", "").toDoubleOrNull() ?: item.minTemp
            val (convertedMaxTemp, _) = convertTemperature(maxTempValue)
            binding.weekTempRange.text = String.format("%.1f - %.1f %s", convertedMinTemp, convertedMaxTemp, tempUnitLabel)
            val iconUrl = "https://openweathermap.org/img/wn/${item.icon}@2x.png"
            Glide.with(binding.weekIcon.context)
                .load(iconUrl)
                .into(binding.weekIcon)
        }
    }
}