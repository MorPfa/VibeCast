package app.vibecast.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import app.vibecast.data.data_repository.data_source.local.LocalWeatherDataSource
import app.vibecast.data.data_repository.data_source.remote.RemoteWeatherDataSource
import app.vibecast.data.data_repository.repository.WeatherRepositoryImpl
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
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class WeatherRepositoryImplTest {

    private val remoteWeatherDataSource = mock<RemoteWeatherDataSource>()
    private val localWeatherDataSource = mock<LocalWeatherDataSource>()
    private val context = mock<Context>()
    private val connectivityManager = mock<ConnectivityManager>()
    private val networkCapabilities = mock<NetworkCapabilities>()
    private val repositoryImpl = WeatherRepositoryImpl(remoteWeatherDataSource,localWeatherDataSource, context)
    private lateinit var  expectedWeather : WeatherDto
    private val cityName = "London"

    @Before
    fun setUp() {

        expectedWeather =  WeatherDto(
            cityName = cityName,
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
    fun testRefreshWeatherWithCity() = runTest {
        whenever(remoteWeatherDataSource.getWeather(expectedWeather.cityName)).thenReturn(flowOf(expectedWeather))
        val result = repositoryImpl.refreshWeather(expectedWeather.cityName).first()
        assertEquals(expectedWeather, result)
        verify(localWeatherDataSource).addWeather(expectedWeather)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testRefreshWeatherWithCoordinates() = runTest {
        whenever(remoteWeatherDataSource.getWeather(expectedWeather.latitude, expectedWeather.longitude)).thenReturn(flowOf(expectedWeather))
        val result = repositoryImpl.refreshWeather(expectedWeather.latitude, expectedWeather.longitude).first()
        assertEquals(expectedWeather, result)
        verify(localWeatherDataSource).addWeather(expectedWeather)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testGetWeatherWithCity() = runTest {
        whenever(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManager)
        whenever(connectivityManager.activeNetwork).thenReturn(mock())
        whenever(connectivityManager.getNetworkCapabilities(any())).thenReturn(networkCapabilities)
        whenever(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(true)
        whenever(remoteWeatherDataSource.getWeather(expectedWeather.cityName)).thenReturn(flowOf(expectedWeather))
        val result = repositoryImpl.getWeather(expectedWeather.cityName).first()
        assertEquals(expectedWeather, result)

    }


    }
