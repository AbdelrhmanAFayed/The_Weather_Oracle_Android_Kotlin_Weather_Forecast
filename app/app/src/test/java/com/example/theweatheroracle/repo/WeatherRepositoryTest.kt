package com.example.theweatheroracle.repo




import com.example.theweatheroracle.model.weather.*
import com.example.theweatheroracle.model.api.WeatherRemoteDataSource
import com.example.theweatheroracle.model.db.weather.WeatherLocalDataSource
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class WeatherRepositoryTest {

    private lateinit var remoteDataSource: WeatherRemoteDataSource
    private lateinit var localDataSource: WeatherLocalDataSource
    private lateinit var repository: WeatherRepository

    private val city1 = City(
        id = 1,
        name = "City1",
        coord = Coord(lat = 0.0, lon = 0.0),
        country = "US",
        population = 100000,
        timezone = 0,
        sunrise = 0L,
        sunset = 0L
    )
    private val city2 = City(
        id = 2,
        name = "City2",
        coord = Coord(lat = 1.0, lon = 1.0),
        country = "CA",
        population = 200000,
        timezone = 0,
        sunrise = 0L,
        sunset = 0L
    )

    @Before
    fun setup() {
        clearAllMocks()

        remoteDataSource = mockk(relaxUnitFun = true)
        localDataSource = mockk(relaxUnitFun = true)

        coEvery { localDataSource.saveCity(any()) } returns Unit
        coEvery { localDataSource.getAllCities() } returnsMany listOf(listOf(city1, city2), listOf(city1, city2))
        coEvery { localDataSource.deleteAllCities() } returns Unit
        coEvery { localDataSource.deleteCityById(any()) } returns Unit

        runBlocking {
            localDataSource.saveCity(city1)
            localDataSource.saveCity(city2)
        }

        repository = WeatherRepositoryImp.getInstance(remoteDataSource, localDataSource)
        println("Setup complete: Cities = ${listOf(city1, city2).map { it.name to it.id }}")
    }

    @Test
    fun deleteAllCities_clearsAllCitiesFromRepo() = runBlocking {
        // Given: A repository with pre-populated cities
        coEvery { localDataSource.getAllCities() } returnsMany listOf(listOf(city1, city2), emptyList())
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
        coEvery { localDataSource.getAllCities() } returnsMany listOf(listOf(city1, city2), listOf(city2))
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