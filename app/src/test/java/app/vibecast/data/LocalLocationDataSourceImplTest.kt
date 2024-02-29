package app.vibecast.data

import app.vibecast.domain.repository.implementation.Unit
import app.vibecast.data.local_data.db.location.dao.LocationDao
import app.vibecast.data.local_data.db.location.model.LocationEntity
import app.vibecast.data.local_data.db.location.model.LocationWithWeatherData
import app.vibecast.data.local_data.db.weather.model.WeatherEntity
import app.vibecast.data.local_data.data_source.weather.LocalLocationDataSourceImpl
import app.vibecast.domain.model.CurrentWeather
import app.vibecast.domain.model.HourlyWeather
import app.vibecast.domain.model.LocationDto
import app.vibecast.domain.model.LocationWithWeatherDataDto
import app.vibecast.domain.model.WeatherDto
import app.vibecast.domain.model.WeatherCondition
import app.vibecast.domain.repository.UnitPreferenceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class LocalLocationDataSourceImplTest {

    private val locationDao = mock<LocationDao>()
    private val dataStore = mock<UnitPreferenceRepository>()
    private val locationDataSource = LocalLocationDataSourceImpl(locationDao, dataStore)
    private lateinit var locationDto : LocationDto
    private lateinit var weatherDto : WeatherDto
    private lateinit var locationWithWeatherData: LocationWithWeatherData
    private lateinit var locationWithWeatherDataDto: LocationWithWeatherDataDto
    private lateinit var locationEntity : LocationEntity
    private lateinit var weatherEntity : WeatherEntity


    @Before
    fun setUp() {

        weatherDto = WeatherDto(
            cityName = "Seattle",
            country = "US",
            latitude = 51.5074,
            longitude = -0.1278,
            dataTimestamp = System.currentTimeMillis(),
            timezone = "US",
            unit = Unit.IMPERIAL,
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
                        mainDescription = "Clear",
                        icon = "01d"
                    )
                )
            ),
            hourlyWeather = List(24) {
                HourlyWeather(
                    timestamp = (1637094000 + it * 3600).toLong(),
                    temperature = 14.0,
                    feelsLike = 13.0,
                    humidity = 65,
                    uvi = 5.5,
                    windSpeed = 11.0,
                    weatherConditions = listOf(
                        WeatherCondition(
                            mainDescription = "Clear",
                            icon = "01d"
                        )
                    ),
                    chanceOfRain = 10.0
                )
            }
        )

        locationDto = LocationDto(
            cityName = "Seattle",
            country = "US"
        )

        locationEntity = LocationEntity(
            cityName = "Seattle",
            country = "US"
        )


        weatherEntity =  WeatherEntity(
                cityName = "Seattle",
                countryName = "US",
                dataTimestamp = 1000,
                unit = Unit.IMPERIAL,
                weatherData = WeatherDto(
                    cityName = "Seattle",
                    country = "US",
                    latitude = 51.5074,
                    longitude = -0.1278,
                    dataTimestamp = 1000,
                    timezone = "US",
                    unit = Unit.IMPERIAL,
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
                            mainDescription = "Clear",
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
                            windSpeed = 11.0,
                            weatherConditions = listOf(
                                WeatherCondition(
                                    mainDescription = "Clear",
                                    icon = "01d"
                                )
                            ),
                            chanceOfRain = 10.0
                        )
                    }
                )
        )

        locationWithWeatherData = LocationWithWeatherData(locationEntity, weatherEntity)

        locationWithWeatherDataDto = LocationWithWeatherDataDto(locationDto, weatherDto)

    }


    @ExperimentalCoroutinesApi
    @Test
    fun testAddLocationWithWeather() {
        runTest {
            val expectedTimestamp = System.currentTimeMillis()
            val timestampRange = expectedTimestamp - 1000..expectedTimestamp + 1000

            // Perform the test
            locationDataSource.addLocationWithWeather(locationWithWeatherDataDto)

            // Verify that the expected method call was made with a dataTimestamp within the range
            verify(locationDao).addLocationWithWeather(
                argThat {
                    assertTrue(weatherEntity.dataTimestamp in timestampRange)
                    true // Always return true to satisfy the suspension function
                },
                eq(weatherEntity) // Use eq() for exact matching of the WeatherEntity
            )
        }
    }




    @ExperimentalCoroutinesApi
    @Test
    fun testAddLocation() {
        runTest {
            locationDataSource.addLocation(locationDto)
            verify(locationDao).addLocation(locationEntity)

        }

    }
    @ExperimentalCoroutinesApi
    @Test
    fun testGetLocationWithWeather() {
        runTest {
            val localList = listOf(locationWithWeatherData)
            val expectedList = listOf(locationWithWeatherDataDto)
            whenever(locationDao.getLocationsWithWeather()).thenReturn(flowOf(localList))
            val result = locationDataSource.getLocationWithWeather().first()
            assertEquals(expectedList, result)

        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testGetLocation() {
        runTest {
            val expectedLocation = locationDto
            val cityName = "London"
            whenever(locationDao.getLocation(cityName)).thenReturn(flowOf(locationEntity))
            val result = locationDataSource.getLocation(cityName).first()
            assertEquals(expectedLocation, result)

        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testGetAllLocations() {
        runTest {
            val expectedList = listOf(locationDto)
            val localList = listOf(locationEntity)
            whenever(locationDao.getLocations()).thenReturn(flowOf(localList))
            val result = locationDataSource.getLocations().first()
            assertEquals(expectedList, result)

        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testDeleteLocation() {
        runTest {
            locationDataSource.deleteLocation(locationDto)
            verify(locationDao).deleteLocation(locationEntity)

        }
    }




}