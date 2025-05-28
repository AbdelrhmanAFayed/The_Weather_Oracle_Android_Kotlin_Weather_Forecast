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

    private lateinit var remoteDataSource: FakeWeatherRemoteDataSource
    private lateinit var localDataSource: FakeWeatherLocalDataSource
    private lateinit var repository: WeatherRepository

    @Before
    fun init() {
        remoteDataSource = FakeWeatherRemoteDataSource(
            weatherResponses = mutableListOf(),
            forecastResponses = mutableListOf()
        )
        localDataSource = FakeWeatherLocalDataSource(
            cities = mutableListOf(),
            forecastEntities = mutableListOf(),
            weatherEntries = mutableListOf()
        )
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
    }

    @Test
    fun deleteAllCities_clearsAllCitiesFromRepo() = runBlocking {
        // Given: A repository with pre-populated cities
        repository = WeatherRepositoryImp.getInstance(remoteDataSource, localDataSource)

        // When: deleteAllCities is called
        runBlocking {
            repository.deleteAllCities()
            delay(2000)
        }

        // Then: All cities are removed from the local data source
        val cities = localDataSource.getAllCities() // Direct access to verify state
        println("Cities after deleteAll: ${cities.map { it.name to it.id }}")
        assertEquals(0, cities.size)
    }

    @Test
    fun deleteCityById_removesCityFromLocalDataSource() = runBlocking {

        // Given: A repository with a pre-populated city
        repository = WeatherRepositoryImp.getInstance(remoteDataSource, localDataSource)



        // When: deleteCityById is called for cityId 1
        repository.deleteCityById(1)

        // Then: The city is removed from the local data source
        val cities = localDataSource.getAllCities() // Direct access to verify state
        println("Cities after delete: ${cities.map { it.name to it.id }}")
        assertEquals(1, cities.size)
    }
}