package com.example.theweatheroracle.repo


import com.example.theweatheroracle.model.weather.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WeatherRepositoryTest {

    private val remoteDataSource = FakeWeatherRemoteDataSource(
        weatherResponses = mutableListOf(),
        forecastResponses = mutableListOf()
    )
    private val localDataSource = FakeWeatherLocalDataSource(
        cities = mutableListOf(),
        forecastEntities = mutableListOf(),
        weatherEntries = mutableListOf()
    )
    private lateinit var repository: WeatherRepository

    @Before
    fun setup() {
        // Clear data source to ensure clean state
        localDataSource.cities.clear()
        localDataSource.forecastEntities.clear()
        localDataSource.weatherEntries.clear()

        // Populate with test data
        runBlocking {
            localDataSource.saveCity(
                City(
                    id = 1,
                    name = "City1",
                    coord = Coord(lat = 0.0, lon = 0.0),
                    country = "US",
                    population = 100000,
                    timezone = 0,
                    sunrise = 0L,
                    sunset = 0L
                )
            )
            localDataSource.saveCity(
                City(
                    id = 2,
                    name = "City2",
                    coord = Coord(lat = 1.0, lon = 1.0),
                    country = "CA",
                    population = 200000,
                    timezone = 0,
                    sunrise = 0L,
                    sunset = 0L
                )
            )
        }

        // Initialize repository with fresh instance
        repository = WeatherRepositoryImp.getInstance(remoteDataSource, localDataSource)
      //  println("Setup complete: Cities = ${localDataSource.getAllCities().map { it.name to it.id }}")
    }

    @Test
    fun deleteAllCities_clearsAllCitiesFromRepo() = runBlocking {
        // Given: A repository with pre-populated cities
        val initialCities = localDataSource.getAllCities()
        println("Before deleteAllCities: Cities = ${initialCities.map { it.name to it.id }}")
        assertEquals("Initial state should have 2 cities", 2, initialCities.size)

        // When: deleteAllCities is called
        repository.deleteAllCities()

        // Then: All cities are removed from the local data source
        val cities = localDataSource.getAllCities()
        println("After deleteAllCities: Cities = ${cities.map { it.name to it.id }}")
        assertEquals(0, cities.size)
    }

    @Test
    fun deleteCityById_removesCityFromLocalDataSource() = runBlocking {
        // Given: A repository with pre-populated cities
        println("Before deleteCityById: Cities = ${localDataSource.getAllCities().map { it.name to it.id }}")

        // When: deleteCityById is called for cityId 1
        repository.deleteCityById(1)

        // Then: The city is removed from the local data source
        val cities = localDataSource.getAllCities()
        println("After deleteCityById: Cities = ${cities.map { it.name to it.id }}")
        assertEquals(1, cities.size)
        assertEquals("City2", cities.firstOrNull()?.name)
    }
}