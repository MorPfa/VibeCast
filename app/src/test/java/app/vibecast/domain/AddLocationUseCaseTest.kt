package app.vibecast.domain

import app.vibecast.data.local.db.location.LocationEntity
import app.vibecast.data.local.db.location.LocationWithWeatherData
import app.vibecast.data.local.db.weather.WeatherEntity
import app.vibecast.domain.entity.CurrentWeather
import app.vibecast.domain.entity.HourlyWeather
import app.vibecast.domain.entity.Location
import app.vibecast.domain.entity.Weather
import app.vibecast.domain.entity.WeatherCondition
import app.vibecast.domain.repository.LocationRepository
import app.vibecast.domain.usecase.AddLocationUseCase
import app.vibecast.domain.usecase.GetLocationUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class AddLocationUseCaseTest {

    private val locationRepository = mock<LocationRepository>()
    private val useCase = AddLocationUseCase(mock(),locationRepository)
    private lateinit var location : Location
    private lateinit var locationWithWeatherData: LocationWithWeatherData
    private lateinit var locationEntity : LocationEntity
    private lateinit var weatherEntity : WeatherEntity
    private lateinit var weather : Weather

    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
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
    @Test
    fun testProcess()= runTest {
        val request = AddLocationUseCase.Request(locationWithWeatherData)
        useCase.process(request)
        verify(locationRepository).addLocationWeather(request.location)
    }
}