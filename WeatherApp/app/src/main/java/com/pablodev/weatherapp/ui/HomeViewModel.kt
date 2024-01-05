package com.pablodev.weatherapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.pablodev.weatherapp.data.ErrorResponse
import com.pablodev.weatherapp.data.WeatherResponse
import com.pablodev.weatherapp.network.NetworkModule
import com.pablodev.weatherapp.utils.CardinalDirection
import com.pablodev.weatherapp.utils.Logger
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class HomeViewModel (private val networkModule: NetworkModule) : ViewModel() {

    private val logger = Logger.getInstance(javaClass)

    private val _weatherResponse = MutableLiveData<WeatherResponse>()
    val weatherResponse: LiveData<WeatherResponse> = _weatherResponse

    private val _error = MutableLiveData<ErrorResponse>()
    val error: LiveData<ErrorResponse> = _error

    fun getWeather(query: String) {

        val zipCode: String? = if (query.isNumeric()) query else null
        val cityName: String? = if (!query.isNumeric()) query else null

        viewModelScope.launch {
            networkModule.getWeather(
                zipCode = zipCode,
                cityName = cityName,
                onSuccess = onWeatherSuccess,
                onError = onWeatherError
            )
        }
    }

    private val onWeatherSuccess: (WeatherResponse?) -> Unit = { response ->
        response?.let {
            _weatherResponse.value = it
        }
    }

    private val onWeatherError: (ErrorResponse) -> Unit = {
        _error.value = it
    }

    suspend fun getWeatherForLocations(locationsMap: Map<CardinalDirection, LatLng>) {
        logger.debug("getWeatherForLocations for $locationsMap")
        val deferredResultsMap = mutableMapOf<CardinalDirection, Deferred<WeatherResponse?>>()

        locationsMap.forEach { (cardinalDirection, latLng) ->
            deferredResultsMap[cardinalDirection] = viewModelScope.async {
                networkModule.getWeatherByCoordinates(
                    latitude = latLng.latitude,
                    longitude = latLng.longitude
                )
            }
        }

        val results = deferredResultsMap.map { (direction, deferredResult) ->
            direction to deferredResult.await()
        }.toMap()

        results.forEach { (direction, result) ->
            logger.debug("For direction $direction weather is $result")
        }
    }

    class HomeViewModelFactory(
        private val networkModule: NetworkModule
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(networkModule) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    private fun String.isNumeric(): Boolean {
        return this.matches("\\d+".toRegex())
    }
}