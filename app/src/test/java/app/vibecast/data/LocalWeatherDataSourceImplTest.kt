package app.vibecast.data



import app.vibecast.data.local.db.weather.WeatherDao
import app.vibecast.data.local.db.weather.WeatherEntity
import app.vibecast.data.local.source.LocalWeatherDataSourceImpl
import app.vibecast.domain.entity.CurrentWeather
import app.vibecast.domain.entity.HourlyWeather
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

class LocalWeatherDataSourceImplTest {

    private val weatherDao = mock<WeatherDao>()
    private val weatherDataSource = LocalWeatherDataSourceImpl(weatherDao)
    private lateinit var localWeather: WeatherEntity
    private lateinit var  expectedWeather : WeatherDto
    private val cityName = "London"


    private fun WeatherEntity.toWeather(): WeatherDto {
        return WeatherDto(
            cityName = cityName,
            latitude = weatherData.latitude,
            longitude = weatherData.longitude,
            currentWeather = weatherData.currentWeather,
            hourlyWeather = weatherData.hourlyWeather
        )
    }

    @Before
    fun setUp() {
         localWeather = WeatherEntity(
            cityName = cityName,
            weatherData = WeatherDto(
                cityName = cityName,
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
        expectedWeather =  WeatherDto(
            cityName = cityName,
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
                    timestamp = 1637094000,
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

    }



    @ExperimentalCoroutinesApi
    @Test
    fun testGetWeather() = runTest {
        whenever(weatherDao.getWeather(cityName)).thenReturn(flowOf(localWeather))
        val result = weatherDataSource.getWeather(cityName).first()
        assertEquals(expectedWeather.latitude,result.latitude)
        assertEquals(expectedWeather.longitude,result.longitude)
        assertEquals(expectedWeather.cityName,result.cityName)
    }





    @ExperimentalCoroutinesApi
    @Test
    fun testAddWeather() = runTest {
        val cityName = "London"
        val weather = localWeather.toWeather()
        val weatherEntity = WeatherEntity(cityName, weather) // Convert weather to WeatherEntity

        weatherDataSource.addWeather(weather)
        verify(weatherDao).addWeather(weatherEntity) // Verify with the correct argument
    }



}