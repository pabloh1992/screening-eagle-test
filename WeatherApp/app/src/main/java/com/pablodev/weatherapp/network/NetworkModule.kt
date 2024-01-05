package com.pablodev.weatherapp.network

import com.google.gson.Gson
import com.pablodev.weatherapp.data.ErrorResponse
import com.pablodev.weatherapp.data.WeatherResponse
import com.pablodev.weatherapp.utils.Logger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NetworkModule(private val api: ApiWeatherService) {

    private val logger = Logger.getInstance(javaClass)
    private val genericError = ErrorResponse("404", "Unknown error")

    private fun <T> callApi(
        call: Call<T>,
        onSuccess: (Response<T>) -> Unit,
        onError: (ErrorResponse) -> Unit
    ) {

        call.enqueue(
            object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    if (response.isSuccessful) {
                        logger.debug("Body ${response.body()}")
                        onSuccess(response)
                    } else {
                        try {
                            val errorBody = response.errorBody()?.string()
                            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                            logger.debug("Error $errorBody")
                            onError(errorResponse ?: genericError)

                        } catch (ex: Exception) {
                            onError(genericError)
                        }
                    }
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    logger.debug(t.toString())
                    onError(ErrorResponse("404", "Check network connection"))
                }
            }
        )
    }

    fun getWeatherByCity(
        cityName : String,
        onSuccess: (WeatherResponse?) -> Unit = {},
        onError: (ErrorResponse) -> Unit = {}) {
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
        onError: (ErrorResponse) -> Unit = {}) {
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