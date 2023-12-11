package app.vibecast.data

import app.vibecast.data.local.db.location.LocationDao
import app.vibecast.data.local.db.location.LocationEntity
import app.vibecast.data.local.db.location.LocationWithWeatherData
import app.vibecast.data.local.db.weather.WeatherEntity
import app.vibecast.data.local.source.LocalLocationDataSourceImpl
import app.vibecast.domain.entity.CurrentWeather
import app.vibecast.domain.entity.HourlyWeather
import app.vibecast.domain.entity.LocationDto
import app.vibecast.domain.entity.LocationWithWeatherDataDto
import app.vibecast.domain.entity.WeatherDto
import app.vibecast.domain.entity.WeatherCondition
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class LocalLocationDataSourceImplTest {

    private val locationDao = mock<LocationDao>()
    private val locationDataSource = LocalLocationDataSourceImpl(locationDao)
    private lateinit var locationDto : LocationDto
    private lateinit var weatherDto : WeatherDto
    private lateinit var locationWithWeatherData: LocationWithWeatherData
    private lateinit var locationWithWeatherDataDto: LocationWithWeatherDataDto
    private lateinit var locationEntity : LocationEntity
    private lateinit var weatherEntity : WeatherEntity


    @Before
    fun setUp() {

        weatherDto = WeatherDto(
            cityName = "London",
            latitude = 51.5074,
            longitude = -0.1278,,
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
                    timestamp = (1637094000 + it * 3600).toLong(),
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

        locationDto = LocationDto(
            cityName = "London",
            locationIndex = 1
        )

        locationEntity = LocationEntity(
            cityName = "London",
            locationIndex = 1)


        weatherEntity =  WeatherEntity(
                cityName = "London",
                weatherData = WeatherDto(
                    cityName = "London",
                    latitude = 51.5074,
                    longitude = -0.1278,,
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

        locationWithWeatherData = LocationWithWeatherData(locationEntity, weatherEntity)

        locationWithWeatherDataDto = LocationWithWeatherDataDto(locationDto, weatherDto)

    }


    @ExperimentalCoroutinesApi
    @Test
    fun testAddLocationWithWeather() {
        runTest {
            locationDataSource.addLocationWithWeather(locationWithWeatherDataDto)
            verify(locationDao).addLocationWithWeather(locationEntity,weatherEntity)

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