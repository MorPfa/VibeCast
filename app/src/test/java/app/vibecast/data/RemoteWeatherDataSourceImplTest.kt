package app.vibecast.data


import app.vibecast.BuildConfig
import app.vibecast.data.remote.network.weather.CoordinateApiModel
import app.vibecast.data.remote.network.weather.CurrentWeatherRemote
import app.vibecast.data.remote.network.weather.HourlyWeatherRemote
import app.vibecast.data.remote.network.weather.WeatherApiModel
import app.vibecast.data.remote.network.weather.WeatherConditionRemote
import app.vibecast.data.remote.network.weather.WeatherService
import app.vibecast.data.remote.source.RemoteWeatherDataSourceImpl
import app.vibecast.domain.entity.CurrentWeather
import app.vibecast.domain.entity.HourlyWeather
import app.vibecast.domain.entity.UseCaseException
import app.vibecast.domain.entity.Weather
import app.vibecast.domain.entity.WeatherCondition
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever


class RemoteWeatherDataSourceImplTest {

    private val weatherService = mock<WeatherService>()
    private val weatherDataSource = RemoteWeatherDataSourceImpl(weatherService)
    private lateinit var remoteWeather: WeatherApiModel
    private lateinit var  expectedWeather : Weather
    private val cityName = "London"


    @Before
    fun setUp() {
         remoteWeather = WeatherApiModel(
            cityName = "London",
            latitude = 51.5074,
            longitude = -0.1278,
            currentWeatherRemote = CurrentWeatherRemote(
                timestamp = 1637094000,
                temperature = 15.0,
                feelsLike = 14.0,
                humidity = 70,
                uvi = 5.2,
                cloudCover = 40,
                visibility = 10,
                windSpeed = 12.0,
                weatherConditionRemotes = listOf(
                    WeatherConditionRemote(
                        conditionId = 800,
                        mainDescription = "Clear",
                        detailedDescription = "Clear sky",
                        icon = "01d"
                    )
                )
            ),
            hourlyWeather = List(24) {
                HourlyWeatherRemote(
                    timestamp = (1637094000 + it * 3600).toLong(),
                    temperature = 14.0,
                    feelsLike = 13.0,
                    humidity = 65,
                    uvi = 5.5,
                    cloudCover = 45,
                    windSpeed = 11.0,
                    weatherConditionRemotes = listOf(
                        WeatherConditionRemote(
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

        expectedWeather =  Weather(
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
    fun testGetCityCoordinates() = runTest {
        val cityName = "London"
        val remoteCoordinates = CoordinateApiModel(51.5073219,-0.1276474 )
        whenever(weatherService.getCiyCoordinates(cityName,1,BuildConfig.OWM_KEY)).thenReturn(remoteCoordinates)
        val result = weatherDataSource.getCoordinates(cityName).first()
        assertEquals(remoteCoordinates, result)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testGetWeather() = runTest {
        val cityName = "London"
        val remoteCoordinates = CoordinateApiModel(51.5073219, -0.1276474)

        whenever(weatherService.getCiyCoordinates(cityName, 1, BuildConfig.OWM_KEY )).thenReturn(remoteCoordinates)

        whenever(weatherService.getWeather(remoteCoordinates.latitude, remoteCoordinates.longitude, BuildConfig.OWM_KEY)).thenReturn(remoteWeather)

        val result = weatherDataSource.getWeather(cityName).first()



        assertEquals(expectedWeather.longitude, result.longitude)
        assertEquals(expectedWeather.latitude, result.latitude)
        assertEquals(expectedWeather.currentWeather?.timestamp, result.currentWeather?.timestamp)
    }


    @ExperimentalCoroutinesApi
    @Test
    fun testGetWeatherThrowsError() = runTest {
        val cityName = "London"
        val remoteCoordinates = CoordinateApiModel(51.5073219,-0.1276474 )
        whenever(weatherService.getWeather(remoteCoordinates.latitude, remoteCoordinates.longitude, BuildConfig.OWM_KEY)).thenThrow(RuntimeException())
        weatherDataSource.getWeather(cityName).catch {
            assertTrue(it is UseCaseException.WeatherException)
        }.collect()
    }
}