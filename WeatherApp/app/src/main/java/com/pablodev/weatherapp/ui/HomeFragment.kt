package com.pablodev.weatherapp.ui

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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
        viewModel.locationsWeatherResponse.observe(viewLifecycleOwner) { results ->

            val directionWithHighestTemp =
                results.maxByOrNull { it.value?.main?.temp ?: Double.MIN_VALUE }?.key
            val directionWithHighestHumidity =
                results.maxByOrNull { it.value?.main?.humidity ?: Int.MIN_VALUE }?.key
            val directionWithHighestWindSpeed =
                results.maxByOrNull { it.value?.wind?.speed ?: Double.MIN_VALUE }?.key
            val directionWithLongestRain =
                results.maxByOrNull { it.value?.rain?.oneHour ?: Double.MIN_VALUE }?.key

            val message = """
                Highest Temperature: ${if(results.all { it.value?.main?.temp == null }) "-" else directionWithHighestTemp}
                Temperature: ${results[directionWithHighestTemp]?.main?.temp?: "-"}
                Name: ${if(results.all { it.value?.main?.temp == null }) "-" else results[directionWithHighestTemp]?.name ?: "-"}
                
                Highest Humidity: ${if(results.all { it.value?.main?.humidity == null }) "-" else directionWithHighestHumidity}
                Humidity: ${results[directionWithHighestHumidity]?.main?.humidity?: "-"}
                Name: ${if(results.all { it.value?.main?.humidity == null }) "-" else results[directionWithHighestHumidity]?.name ?: "-"}
                
                Highest Wind Speed: ${if(results.all { it.value?.wind?.speed == null }) "-" else directionWithHighestWindSpeed}
                Wind Speed: ${results[directionWithHighestWindSpeed]?.wind?.speed?: "-"}
                Name: ${if(results.all { it.value?.wind?.speed == null }) "-" else results[directionWithHighestWindSpeed]?.name ?: "-"}
                
                Longest Rain: ${if(results.all { it.value?.rain?.oneHour == null }) "-" else directionWithLongestRain}
                Rain Time: ${results[directionWithLongestRain]?.rain?.oneHour ?: "-"}
                Name: ${if(results.all { it.value?.rain?.oneHour == null }) "-" else results[directionWithLongestRain]?.name ?: "-"}
           """.trimIndent()

            showDialog(message)
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
            mMap.addMarker(MarkerOptions().position(it).title(cityName))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, Constants.DEFAULT_ZOOM))
            mMap.setOnMarkerClickListener { _ ->
                addFourLocations(it)
                false
            }
        }
    }

    private fun addFourLocations(mainLocation: LatLng) {
        val locationMap = mutableMapOf<CardinalDirection, LatLng>()
        for (direction in CardinalDirection.entries) {
            val location = mainLocation.calculateDestination(Constants.DEFAULT_DISTANCE, direction.bearing)
            mMap.addMarker(MarkerOptions().position(location))
            locationMap[direction] = location
        }
        lifecycleScope.launch {
            viewModel.getWeatherForLocations(locationMap)
        }
    }

    private fun showErrorSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showDialog(message: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Weather Information")
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
}