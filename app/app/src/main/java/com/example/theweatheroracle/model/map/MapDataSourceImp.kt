package com.example.theweatheroracle.model.map

import org.osmdroid.util.GeoPoint
import retrofit2.HttpException

object MapDataSourceImp : IMapDataSource {

    private val nominatimService: NominatimService = MapRetrofitClient.mapService

    override suspend fun searchCityByName(cityName: String): Result<GeoPoint> {
        return try {
            val response = nominatimService.searchCity(query = cityName.replace(" ", "+"))
            if (response.isNotEmpty()) {
                val result = response[0]
                Result.success(GeoPoint(result.lat.toDouble(), result.lon.toDouble()))
            } else {
                Result.failure(Exception("City not found"))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string() ?: "No error details"
            Result.failure(Exception("HTTP ${e.code()}: $errorBody"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}