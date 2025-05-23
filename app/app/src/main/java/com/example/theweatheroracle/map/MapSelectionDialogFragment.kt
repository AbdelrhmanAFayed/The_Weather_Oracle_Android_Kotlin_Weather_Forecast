package com.example.theweatheroracle.map

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.theweatheroracle.R
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay


class MapSelectionDialogFragment : DialogFragment() {
    private var onLocationSelected: ((Double, Double) -> Unit)? = null

    companion object {
        fun newInstance(onLocationSelected: (Double, Double) -> Unit): MapSelectionDialogFragment {
            return MapSelectionDialogFragment().apply {
                this.onLocationSelected = onLocationSelected
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Configuration.getInstance().load(requireContext(), requireActivity().getSharedPreferences("osmdroid", 0))
        val view = inflater.inflate(R.layout.fragment_map_selection_dialog, container, false)

        val mapView = view.findViewById<MapView>(R.id.map_view)
        val confirmButton = view.findViewById<Button>(R.id.confirm_button)

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(GeoPoint(29.9978, 31.0529))

        var selectedLocation: GeoPoint? = null

        val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                selectedLocation = p
                Log.d("MapSelection", "Location tapped: $p")
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        })
        mapView.overlays.add(mapEventsOverlay)

        confirmButton.setOnClickListener {
            if (selectedLocation != null) {
                onLocationSelected?.invoke(selectedLocation!!.latitude, selectedLocation!!.longitude)
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Please tap on the map to select a location", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(), // 90% of screen width
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onResume() {
        super.onResume()
        view?.findViewById<MapView>(R.id.map_view)?.onResume()
    }

    override fun onPause() {
        super.onPause()
        view?.findViewById<MapView>(R.id.map_view)?.onPause()
    }
}