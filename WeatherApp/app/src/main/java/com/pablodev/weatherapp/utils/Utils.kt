package com.pablodev.weatherapp.utils

import com.google.android.gms.maps.model.LatLng
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

fun LatLng.calculateDestination(distance: Double, bearing: Double): LatLng {
    // Earth radius in meters
    val earthRadius = 6371000.0

    // Convert distance to radians
    val distanceRadians = distance / earthRadius

    // Convert bearing to radians
    val bearingRadians = Math.toRadians(bearing)

    // Convert current latitude and longitude to radians
    val currentLatRadians = Math.toRadians(latitude)
    val currentLngRadians = Math.toRadians(longitude)

    // Calculate new latitude
    val newLatRadians = asin(sin(currentLatRadians) * cos(distanceRadians) +
            cos(currentLatRadians) * sin(distanceRadians) * cos(bearingRadians))

    // Calculate new longitude
    val newLngRadians = currentLngRadians + atan2(
        sin(bearingRadians) * sin(distanceRadians) * cos(currentLatRadians),
        cos(distanceRadians) - sin(currentLatRadians) * sin(newLatRadians))

    // Convert new latitude and longitude back to degrees
    val newLat = Math.toDegrees(newLatRadians)
    val newLng = Math.toDegrees(newLngRadians)

    return LatLng(newLat, newLng)
}