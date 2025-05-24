package com.example.theweatheroracle.fav

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.theweatheroracle.databinding.FavouritesItemBinding
import com.example.theweatheroracle.model.settings.ISettingsManager

class FavouritesAdapter(
    private val onCityClicked: (Int) -> Unit,
    private val settingsManager: ISettingsManager ) :
    ListAdapter<CityWithTemperature, FavouritesAdapter.ViewHolder>(DiffCallback()) {
    class ViewHolder(val binding: FavouritesItemBinding) : RecyclerView.ViewHolder(binding.root)

    class DiffCallback : DiffUtil.ItemCallback<CityWithTemperature>() {
        override fun areItemsTheSame(oldItem: CityWithTemperature, newItem: CityWithTemperature) =
            oldItem.city.id == newItem.city.id

        override fun areContentsTheSame(oldItem: CityWithTemperature, newItem: CityWithTemperature) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FavouritesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.apply {
            binding.cityNameText.text = item.city.name
            binding.tempText.text = if (item.latestTemperature != null) {
                val tempUnit = settingsManager.getTemperatureUnit()
                val (convertedTemp, tempUnitLabel) = when (tempUnit.lowercase()) {
                    "celsius" -> (item.latestTemperature - 273.15) to "°C"
                    "fahrenheit" -> ((item.latestTemperature - 273.15) * 9 / 5 + 32) to "°F"
                    else -> item.latestTemperature to "K"
                }
                String.format("%.1f %s", convertedTemp, tempUnitLabel)
            } else {
                "N/A"
            }

            binding.root.setOnClickListener {
                onCityClicked(item.city.id)
            }
        }
    }

}