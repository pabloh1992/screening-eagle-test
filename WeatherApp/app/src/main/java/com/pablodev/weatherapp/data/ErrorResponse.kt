package com.pablodev.weatherapp.data

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("cod")
    val cod: String?,

    @SerializedName("message")
    val message: String?
)