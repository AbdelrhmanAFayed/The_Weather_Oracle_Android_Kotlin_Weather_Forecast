package com.example.theweatheroracle.model.map

import org.osmdroid.util.GeoPoint

sealed class SearchResult {
    data class Success(val location: GeoPoint) : SearchResult()
    object NotFound : SearchResult()
    data class Error(val message: String) : SearchResult()
}