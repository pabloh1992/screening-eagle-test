package com.pablodev.weatherapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pablodev.weatherapp.data.WeatherResponse
import com.pablodev.weatherapp.network.NetworkModule
import com.pablodev.weatherapp.utils.Logger
import kotlinx.coroutines.launch

class HomeViewModel constructor(private val repository: NetworkModule) : ViewModel() {

    private val logger = Logger.getInstance(javaClass)

    private val _weatherResponse = MutableLiveData<WeatherResponse>()
    val weatherResponse: LiveData<WeatherResponse> = _weatherResponse

    fun getWeatherByCity(cityName: String) {
        viewModelScope.launch {
            repository.getWeatherByCity(
                cityName = cityName,
                onSuccess = { response ->
                    response?.let {
                        _weatherResponse.value = it
                    }
                }
            )
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
}