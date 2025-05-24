package com.example.theweatheroracle.fav

import android.annotation.SuppressLint
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.theweatheroracle.R
import com.example.theweatheroracle.databinding.FragmentFavBinding
import com.example.theweatheroracle.model.WeatherRepositoryImp
import com.example.theweatheroracle.model.api.WeatherRemoteDataSourceImpl
import com.example.theweatheroracle.model.db.WeatherLocalDataSourceImpl
import com.example.theweatheroracle.model.settings.ISettingsManager
import com.example.theweatheroracle.model.settings.SettingsManager

class FavFragment : Fragment() {

    private lateinit var binding: FragmentFavBinding
    private lateinit var viewModel: FavouritesViewModel
    private lateinit var adapter: FavouritesAdapter
    private lateinit var settingsManager: ISettingsManager

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
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
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
                viewModel.removeCityFromFavorites(city.id)
            }
        }
        ItemTouchHelper(swipeToDeleteCallback).attachToRecyclerView(binding.favouritesRecyclerView)

        viewModel.favoriteCities.observe(viewLifecycleOwner) { cities ->
            adapter.submitList(cities)
            adapter.notifyDataSetChanged()
        }
    }
    override fun onResume() {
        super.onResume()
        viewModel.fetchFavoriteCities()
    }


}