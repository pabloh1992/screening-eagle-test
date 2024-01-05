package com.pablodev.weatherapp.data

import com.google.android.gms.maps.model.LatLng

data class Coord(
    val lat: Double?,
    val lon: Double?
)

fun Coord.toLatLng(): LatLng? {
    return if (lat != null && lon != null) {
        LatLng(lat, lon)
    } else {
        null
    }
}