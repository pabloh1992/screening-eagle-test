package com.pablodev.weatherapp.network

import com.pablodev.weatherapp.Constants
import com.pablodev.weatherapp.data.WeatherResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiWeatherService {

    @GET("weather")
    fun getWeather(
        @Query("q") cityName: String?,
        @Query("zip") zipCode: String?,
        @Query("units") units: String = Constants.DEFAULT_TEMP_UNITS,
        @Query("appid") apiKey: String = Constants.API_KEY
    ): Call<WeatherResponse>

    @GET("weather")
    suspend fun getWeatherByCoordinates(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") units: String = Constants.DEFAULT_TEMP_UNITS,
        @Query("appid") apiKey: String = Constants.API_KEY
    ): Response<WeatherResponse>
}