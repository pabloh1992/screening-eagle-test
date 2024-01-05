package com.pablodev.weatherapp.network

import com.pablodev.weatherapp.data.WeatherResponse
import com.pablodev.weatherapp.utils.Logger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NetworkModule(private val api: ApiWeatherService) {

    private val logger = Logger.getInstance(javaClass)

    private fun <T> callApi(
        call: Call<T>,
        onSuccess: (Response<T>) -> Unit,
        onError: (String) -> Unit
    ) {

        call.enqueue(
            object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    if (response.isSuccessful) {
                        logger.debug("Body ${response.body()}")
                        onSuccess(response)
                    } else {
                        try {
                            val error = response.errorBody()?.string()
                            logger.debug("Error ${response.errorBody()}")
                            onError(error ?: "onError")
                        } catch (ex: Exception) {
                            onError("onError")
                        }
                    }
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    logger.debug(t.toString())
                    onError("onError")
                }
            }
        )
    }

    fun getWeatherByCity(
        cityName : String,
        onSuccess: (WeatherResponse?) -> Unit = {},
        onError: (String) -> Unit = {}) {
        callApi(
            call = api.getWeatherByCity(cityName = cityName),
            onSuccess = { response ->
                response.body()?.let {
                    onSuccess(it)
                }
            },
            onError = {
                onError(it)
            }
        )
    }

    fun getWeatherByZipCode(
        zipCode : String,
        onSuccess: (WeatherResponse?) -> Unit = {},
        onError: (String) -> Unit = {}) {
        callApi(
            call = api.getWeatherByZipCode(zipCode = zipCode),
            onSuccess = { response ->
                response.body()?.let {
                    onSuccess(it)
                }
            },
            onError = {
                onError(it)
            }
        )
    }
}