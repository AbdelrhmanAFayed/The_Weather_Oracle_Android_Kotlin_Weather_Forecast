package com.example.theweatheroracle.fav

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.theweatheroracle.R
import com.example.theweatheroracle.databinding.FragmentFavBinding
import com.example.theweatheroracle.nav.SharedViewModel
import com.example.theweatheroracle.model.weather.WeatherRepositoryImp
import com.example.theweatheroracle.model.api.WeatherRemoteDataSourceImpl
import com.example.theweatheroracle.model.db.weather.WeatherLocalDataSourceImpl
import com.example.theweatheroracle.model.settings.ISettingsManager
import com.example.theweatheroracle.model.settings.SettingsManager

class FavFragment : Fragment() {

    private lateinit var binding: FragmentFavBinding
    private lateinit var viewModel: FavouritesViewModel
    private lateinit var adapter: FavouritesAdapter
    private lateinit var settingsManager: ISettingsManager
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsManager = SettingsManager(requireContext())
        val factory = FavouritesViewModelFactory(
            WeatherRepositoryImp.getInstance(
                WeatherRemoteDataSourceImpl,
                WeatherLocalDataSourceImpl.getInstance(requireContext())
            ),
            settingsManager
        )
        viewModel = ViewModelProvider(this, factory)[FavouritesViewModel::class.java]
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = FavouritesAdapter(
            onCityClicked = { cityId ->
                viewModel.setCurrentCity(cityId)
                findNavController().navigate(R.id.nav_home)
            },
            settingsManager = settingsManager
        )

        binding.favouritesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@FavFragment.adapter
        }

        val swipeToDeleteCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val city = adapter.currentList[position].city
                AlertDialog.Builder(requireContext())
                    .setMessage(getString(R.string.confirm_delete_city, city.name))
                    .setPositiveButton(R.string.delete) { _, _ ->
                        viewModel.removeCityFromFavorites(city.id)
                        Log.d("FavFragment", "Deleted city: ${city.name}, id: ${city.id}")
                    }
                    .setNegativeButton(R.string.cancel) { _, _ ->
                        adapter.notifyItemChanged(position)
                        Log.d("FavFragment", "Cancelled deletion for city: ${city.name}")
                    }
                    .setCancelable(false)
                    .show()
            }
        }
        ItemTouchHelper(swipeToDeleteCallback).attachToRecyclerView(binding.favouritesRecyclerView)

        viewModel.favoriteCities.observe(viewLifecycleOwner) { cities ->
            Log.d("FavFragment", "Favorite cities updated: ${cities.map { it.city.name }}")
            adapter.submitList(cities)

        }

        sharedViewModel.refreshFavorites.observe(viewLifecycleOwner) {
            Log.d("FavFragment", "RefreshFavorites observed: Fetching favorite cities")
            viewModel.fetchFavoriteCities()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("FavFragment", "onResume: Fetching favorite cities")
        viewModel.fetchFavoriteCities()
    }
}