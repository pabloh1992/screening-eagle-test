package com.pablodev.weatherapp.data

data class WeatherResponse(
    val base: String?,
    val clouds: Clouds?,
    val cod: Int?,
    val coord: Coord?,
    val dt: Int?,
    val id: Int?,
    val main: Main?,
    val name: String?,
    val sys: Sys?,
    val timezone: Int?,
    val visibility: Int?,
    val weather: List<Weather?>? = listOf(),
    val wind: Wind?,
    val rain: Rain?
)