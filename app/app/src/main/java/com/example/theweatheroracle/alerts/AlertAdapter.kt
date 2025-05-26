package com.example.theweatheroracle.alerts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.theweatheroracle.R
import com.example.theweatheroracle.model.alert.Alert
import java.text.SimpleDateFormat
import java.util.Locale

class AlertAdapter(
    private val onDeleteClick: (Alert) -> Unit
) : ListAdapter<Alert, AlertAdapter.AlertViewHolder>(AlertDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.alert_item, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = getItem(position)
        holder.bind(alert, onDeleteClick)
    }

    class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val locationText: TextView = itemView.findViewById(R.id.locationText)
        private val dateTimeText: TextView = itemView.findViewById(R.id.dateTimeText)
        private val typeText: TextView = itemView.findViewById(R.id.typeText)
        private val deleteButton: TextView = itemView.findViewById(R.id.deleteButton)

        fun bind(alert: Alert, onDeleteClick: (Alert) -> Unit) {
            locationText.text = if (alert.cityId != null) "City ID: ${alert.cityId}" else "Lat: ${alert.latitude}, Lon: ${alert.longitude}"
            dateTimeText.text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(alert.dateTime)
            typeText.text = alert.type
            deleteButton.setOnClickListener { onDeleteClick(alert) }
        }
    }
}

class AlertDiffCallback : DiffUtil.ItemCallback<Alert>() {
    override fun areItemsTheSame(oldItem: Alert, newItem: Alert): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Alert, newItem: Alert): Boolean {
        return oldItem == newItem
    }
}