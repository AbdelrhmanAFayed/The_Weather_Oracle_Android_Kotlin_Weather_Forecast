package com.example.theweatheroracle.model.map

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NominatimService {
    @GET("search")
    suspend fun searchCity(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 1,
        @Header("User-Agent") userAgent: String = "WeatherOracleApp/1.0 (com.example.theweatheroracle; abdelrhmanfayed2002@gmail.com)",
        @Header("Referer") referer: String = "https://weatheroracle.app"
    ): List<NominatimResponse>
}