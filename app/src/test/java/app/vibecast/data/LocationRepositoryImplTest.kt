package app.vibecast.data

import app.vibecast.data.data_repository.data_source.local.LocalLocationDataSource
import app.vibecast.data.data_repository.data_source.remote.RemoteWeatherDataSource
import app.vibecast.data.data_repository.repository.LocationRepositoryImpl
import app.vibecast.data.local.db.location.LocationEntity
import app.vibecast.data.local.db.location.LocationWithWeatherData
import app.vibecast.data.local.db.weather.WeatherEntity
import app.vibecast.domain.entity.CurrentWeather
import app.vibecast.domain.entity.HourlyWeather
import app.vibecast.domain.entity.Location
import app.vibecast.domain.entity.Weather
import app.vibecast.domain.entity.WeatherCondition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class LocationRepositoryImplTest {

    private val localLocationDataSource = mock<LocalLocationDataSource>()
    private val remoteWeatherDataSource = mock<RemoteWeatherDataSource>()
    private val locationRepository = LocationRepositoryImpl(localLocationDataSource, remoteWeatherDataSource)
    private lateinit var location : Location
    private lateinit var locationWithWeatherData: LocationWithWeatherData
    private lateinit var locationEntity : LocationEntity
    private lateinit var weatherEntity : WeatherEntity
    private lateinit var weather : Weather
    @ExperimentalCoroutinesApi
    private val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()
    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        location = Location(
            cityName = "London",
            locationIndex = 1
        )

        locationEntity = LocationEntity(
            cityname = "London",
            locationIndex = 1)


        weatherEntity =  WeatherEntity(
            cityName = "London",
            weatherData = Weather(
                cityName = "London",
                latitude = 51.5074,
                longitude = -0.1278,
                currentWeather = CurrentWeather(
                    timestamp = 1637094000,
                    temperature = 15.0,
                    feelsLike = 14.0,
                    humidity = 70,
                    uvi = 5.2,
                    cloudCover = 40,
                    visibility = 10,
                    windSpeed = 12.0,
                    weatherConditions = listOf(
                        WeatherCondition(
                            conditionId = 800,
                            mainDescription = "Clear",
                            detailedDescription = "Clear sky",
                            icon = "01d"
                        )
                    )
                ),
                hourlyWeather = List(24) {
                    HourlyWeather(
                        timestamp = (1637094000 + it * 3600).toLong(), // Incrementing timestamp for hourly forecast
                        temperature = 14.0,
                        feelsLike = 13.0,
                        humidity = 65,
                        uvi = 5.5,
                        cloudCover = 45,
                        windSpeed = 11.0,
                        weatherConditions = listOf(
                            WeatherCondition(
                                conditionId = 800,
                                mainDescription = "Clear",
                                detailedDescription = "Clear sky",
                                icon = "01d"
                            )
                        ),
                        chanceOfRain = 10.0
                    )
                }
            )
        )
        weather = Weather(
                cityName = "London",
                latitude = 51.5074,
                longitude = -0.1278,
                currentWeather = CurrentWeather(
                    timestamp = 1637094000,
                    temperature = 15.0,
                    feelsLike = 14.0,
                    humidity = 70,
                    uvi = 5.2,
                    cloudCover = 40,
                    visibility = 10,
                    windSpeed = 12.0,
                    weatherConditions = listOf(
                        WeatherCondition(
                            conditionId = 800,
                            mainDescription = "Clear",
                            detailedDescription = "Clear sky",
                            icon = "01d"
                        )
                    )
                ),
                hourlyWeather = List(24) {
                    HourlyWeather(
                        timestamp = (1637094000 + it * 3600).toLong(), // Incrementing timestamp for hourly forecast
                        temperature = 14.0,
                        feelsLike = 13.0,
                        humidity = 65,
                        uvi = 5.5,
                        cloudCover = 45,
                        windSpeed = 11.0,
                        weatherConditions = listOf(
                            WeatherCondition(
                                conditionId = 800,
                                mainDescription = "Clear",
                                detailedDescription = "Clear sky",
                                icon = "01d"
                            )
                        ),
                        chanceOfRain = 10.0
                    )
                }
            )


        locationWithWeatherData = LocationWithWeatherData(locationEntity, weatherEntity)

    }

    @ExperimentalCoroutinesApi
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testRefreshLocationWithWeather() {
        runTest {
            val cityName = "London"
            val expectedList = listOf(locationWithWeatherData)
            whenever(localLocationDataSource.getLocationWithWeather()).thenReturn(flowOf(expectedList))
            whenever(remoteWeatherDataSource.getWeather(cityName)).thenReturn(flowOf(weather))
            locationRepository.refreshLocationWeather()
            verify(remoteWeatherDataSource).getWeather(cityName)
            verify(localLocationDataSource).addLocationWithWeather(locationWithWeatherData)
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testAddLocationWithWeather() {
        runTest {
            locationRepository.addLocationWeather(locationWithWeatherData)
            verify(localLocationDataSource).addLocationWithWeather(locationWithWeatherData)
        }
    }


    @ExperimentalCoroutinesApi
    @Test
    fun testGetLocationWeather() {
        runTest {
            val index = 0
            val expectedWeather = weather
            val expectedList = listOf(locationWithWeatherData)
            whenever(localLocationDataSource.getLocationWithWeather()).thenReturn(flowOf(expectedList))
            val result = locationRepository.getLocationWeather(index).first()
            assertEquals(expectedWeather, result)
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testGetAllLocations() {
        runTest {
            val expectedList = listOf(location)
            whenever(localLocationDataSource.getAllLocations()).thenReturn(flowOf(expectedList))
            val result = locationRepository.getLocations().first()
            assertEquals(expectedList, result)
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testGetLocation() {
        runTest {
            val cityName = "London"
            val expectedLocation = location
            whenever(localLocationDataSource.getLocation(cityName)).thenReturn(flowOf(location))
            val result = locationRepository.getLocation(cityName).first()
            assertEquals(expectedLocation, result)
        }
    }


    @ExperimentalCoroutinesApi
    @Test
    fun testAddLocation() {
        runTest {
            locationRepository.addLocation(location)
            verify(localLocationDataSource).addLocation(location)
        }
    }
    @ExperimentalCoroutinesApi
    @Test
    fun testDeleteLocation() {
       runTest {
           locationRepository.deleteLocation(location)
           verify(localLocationDataSource).deleteLocation(location)
       }
   }
}