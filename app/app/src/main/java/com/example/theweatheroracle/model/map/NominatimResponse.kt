package com.example.theweatheroracle.model.map

import com.google.gson.annotations.SerializedName

data class NominatimResponse(
    @SerializedName("lat") val lat: String,
    @SerializedName("lon") val lon: String,
    @SerializedName("display_name") val displayName: String
)