package com.pablodev.weatherapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.pablodev.weatherapp.Constants
import com.pablodev.weatherapp.R
import com.pablodev.weatherapp.data.WeatherResponse
import com.pablodev.weatherapp.data.toLatLng
import com.pablodev.weatherapp.databinding.FragmentHomeBinding
import com.pablodev.weatherapp.network.ApiWeatherModule
import com.pablodev.weatherapp.network.NetworkModule
import com.pablodev.weatherapp.utils.CardinalDirection
import com.pablodev.weatherapp.utils.Logger
import com.pablodev.weatherapp.utils.calculateDestination

class HomeFragment : Fragment(), OnMapReadyCallback {

    private val binding by lazy {
        FragmentHomeBinding.inflate(layoutInflater)
    }

    private val logger = Logger.getInstance(javaClass)

    private val viewModel : HomeViewModel by viewModels {
        HomeViewModel.HomeViewModelFactory(
            networkModule = NetworkModule(
                api = ApiWeatherModule.provideApiService()
            )
        )
    }

    private lateinit var mMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpView()
        setUpObservers()
    }

    private fun setUpView() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.apply {
            searchInputLayout.setEndIconOnClickListener {
                val searchQuery = searchEditText.text.toString()
                viewModel.getWeather(query = searchQuery)
            }
        }
    }

    private fun setUpObservers() {
        viewModel.weatherResponse.observe(viewLifecycleOwner) {
            logger.debug("Weather response = $it")
            updateCityMarker(it)
        }
        viewModel.error.observe(viewLifecycleOwner) {
            showErrorSnackbar("Error: ${it.message}")
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        logger.debug("onMapReady")
        mMap = googleMap
    }

    private fun updateCityMarker(weatherResponse: WeatherResponse) {
        val location = weatherResponse.coord?.toLatLng()
        val cityName = weatherResponse.name
        location?.let {
            mMap.clear()
            val cityMarker = mMap.addMarker(MarkerOptions().position(it).title(cityName))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, Constants.DEFAULT_ZOOM))
            mMap.setOnMarkerClickListener { _ ->
                addFourLocations(it)
                false
            }
            //addFourLocations(it)
        }
    }

    private fun addFourLocations(mainLocation: LatLng) {
        for (direction in CardinalDirection.entries) {
            mMap.addMarker(
                MarkerOptions().position(
                    mainLocation.calculateDestination(Constants.DEFAULT_DISTANCE, direction.bearing)
                )
            )
        }
    }

    private fun showErrorSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}