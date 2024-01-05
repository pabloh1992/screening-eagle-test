package com.pablodev.weatherapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pablodev.weatherapp.data.ErrorResponse
import com.pablodev.weatherapp.data.WeatherResponse
import com.pablodev.weatherapp.network.NetworkModule
import com.pablodev.weatherapp.utils.Logger
import kotlinx.coroutines.launch

class HomeViewModel (private val networkModule: NetworkModule) : ViewModel() {

    private val logger = Logger.getInstance(javaClass)

    private val _weatherResponse = MutableLiveData<WeatherResponse>()
    val weatherResponse: LiveData<WeatherResponse> = _weatherResponse

    private val _error = MutableLiveData<ErrorResponse>()
    val error: LiveData<ErrorResponse> = _error

    fun getWeather(query: String) {
        if (query.isNumeric()) {
            logger.debug("Searching ZipCode = $query")
            viewModelScope.launch {
                networkModule.getWeatherByZipCode(
                    zipCode = query,
                    onSuccess = onWeatherSuccess,
                    onError = onWeatherError
                )
            }

        } else {
            logger.debug("Searching cityName = $query")
            viewModelScope.launch {
                networkModule.getWeatherByCity(
                    cityName = query,
                    onSuccess = onWeatherSuccess,
                    onError = onWeatherError
                )
            }
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