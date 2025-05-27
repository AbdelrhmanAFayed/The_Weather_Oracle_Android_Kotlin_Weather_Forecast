package com.example.theweatheroracle.map

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.theweatheroracle.R
import com.example.theweatheroracle.databinding.FragmentMapSelectionDialogBinding
import com.example.theweatheroracle.model.map.MapDataSourceImp
import com.example.theweatheroracle.model.weather.WeatherRepositoryImp
import com.example.theweatheroracle.model.api.WeatherRemoteDataSourceImpl
import com.example.theweatheroracle.model.db.weather.WeatherLocalDataSourceImpl
import com.example.theweatheroracle.model.map.SearchResult
import com.example.theweatheroracle.model.settings.SettingsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

class MapSelectionDialogFragment(
    private var onLocationSelected: ((Double, Double) -> Unit)? = null
): DialogFragment() {

    private lateinit var viewModel: MapViewModel
    private lateinit var binding: FragmentMapSelectionDialogBinding
    private var currentMarker: Marker? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
        viewModel = ViewModelProvider(
            this,
            MapViewModelFactory(
                WeatherRepositoryImp.getInstance(
                    WeatherRemoteDataSourceImpl,
                    WeatherLocalDataSourceImpl.getInstance(requireContext())
                ),
                SettingsManager(requireContext()),
                MapDataSourceImp
            )
        )[MapViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapSelectionDialogBinding.inflate(inflater, container, false)

        Configuration.getInstance().load(requireContext(), requireActivity().getSharedPreferences("osmdroid", 0))

        binding.mapView.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapView.setMultiTouchControls(true)
        binding.mapView.controller.setZoom(10.0)

        binding.mapView.controller.setCenter(viewModel.getInitialMapCenter())

        val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                if (p != null) {
                    viewModel.setSelectedLocation(p)
                    Log.d("MapSelection", "Location tapped: $p")
                }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }


        })
        binding.mapView.overlays.add(mapEventsOverlay)

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    viewModel.searchCityByName(query.trim())
                    binding.searchView.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        binding.confirmButton.setOnClickListener {
            val selected = viewModel.selectedLocation.value
            if (selected != null) {
                viewModel.addCityFromMap(selected.latitude, selected.longitude)
                onLocationSelected?.invoke(selected.latitude, selected.longitude)
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Please select a location", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.searchResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is SearchResult.Success -> {
                    viewModel.setSelectedLocation(result.location)
                    binding.mapView.controller.setCenter(result.location)
                    Toast.makeText(requireContext(), "Found: ${binding.searchView.query}", Toast.LENGTH_SHORT).show()
                }
                is SearchResult.NotFound -> {
                    Toast.makeText(requireContext(), "City not found", Toast.LENGTH_SHORT).show()
                }
                is SearchResult.Error -> {
                    Toast.makeText(requireContext(), "Search failed: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.selectedLocation.observe(viewLifecycleOwner) { geoPoint ->
            updateMarker(geoPoint)
        }

        return binding.root
    }

    private fun updateMarker(geoPoint: GeoPoint?) {
        currentMarker?.let { binding.mapView.overlays.remove(it) }
        if (geoPoint != null) {
            currentMarker = Marker(binding.mapView).apply {
                position = geoPoint
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Selected Location"
            }
            binding.mapView.overlays.add(currentMarker)
        }
        binding.mapView.invalidate()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }
}