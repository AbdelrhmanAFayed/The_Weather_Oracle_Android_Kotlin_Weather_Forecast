package com.example.theweatheroracle.model.map

import org.osmdroid.util.GeoPoint

interface IMapDataSource {
    suspend fun searchCityByName(cityName: String): Result<GeoPoint>
}