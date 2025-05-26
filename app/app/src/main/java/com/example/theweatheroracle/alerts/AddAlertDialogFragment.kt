package com.example.theweatheroracle.alerts

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.example.theweatheroracle.databinding.DialogAddAlertBinding
import com.example.theweatheroracle.model.alert.Alert
import com.example.theweatheroracle.model.weather.City
import java.util.Calendar

class AddAlertDialogFragment(
    private val favorites: List<City>,
    private val onAddAlert: (Alert) -> Unit
) : DialogFragment() {

    private lateinit var binding: DialogAddAlertBinding
    private var selectedDateTime: Long = 0L

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddAlertBinding.inflate(layoutInflater)

        setupCitySpinner()
        setupDateTimePickers()
        setupTypeSpinner()
        setupButtons()

        return Dialog(requireContext()).apply {
            setContentView(binding.root)
            setTitle("Add Weather Alert")
        }
    }

    private fun setupCitySpinner() {
        val cityNames = favorites.map { it.name }.toMutableList()
        cityNames.add(0, "Select a city")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cityNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.citySpinner.adapter = adapter
    }

    private fun setupDateTimePickers() {
        val calendar = Calendar.getInstance()

        binding.dateButton.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    binding.dateButton.text = "$year-${month + 1}-$dayOfMonth"
                    updateSelectedDateTime(calendar)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.timeButton.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    binding.timeButton.text = "$hourOfDay:$minute"
                    updateSelectedDateTime(calendar)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }
        binding.saveButton
    }

    private fun setupTypeSpinner() {
        val types = listOf("notification", "popup")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.typeSpinner.adapter = adapter
    }

    private fun setupButtons() {
        binding.saveButton.setOnClickListener {
            val selectedCityPosition = binding.citySpinner.selectedItemPosition
            if (selectedCityPosition == 0 || selectedDateTime == 0L) {
                return@setOnClickListener
            }

            val city = favorites[selectedCityPosition - 1]
            val type = binding.typeSpinner.selectedItem.toString()
            val alert = Alert(
                id = 0,
                cityId = city.id,
                latitude = city.coord.lat,
                longitude = city.coord.lon,
                dateTime = selectedDateTime,
                type = type
            )
            onAddAlert(alert)
            dismiss()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }

    private fun updateSelectedDateTime(calendar: Calendar) {
        selectedDateTime = calendar.timeInMillis
    }
}