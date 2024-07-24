package app.vibecast.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import app.vibecast.data.local_data.data_source.weather.LocalWeatherDataSource
import app.vibecast.data.remote_data.data_source.weather.RemoteWeatherDataSource
import app.vibecast.domain.repository.weather.Unit
import app.vibecast.domain.repository.weather.WeatherRepositoryImpl
import app.vibecast.domain.model.CurrentWeather
import app.vibecast.domain.model.HourlyWeather
import app.vibecast.domain.model.LocationDto
import app.vibecast.domain.model.LocationWithWeatherDataDto
import app.vibecast.domain.model.WeatherDto
import app.vibecast.domain.model.WeatherCondition
import app.vibecast.domain.repository.weather.UnitPreferenceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
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
    private val dataStore = mock<UnitPreferenceRepository>()
    private val context = mock<Context>()
    private val connectivityManager = mock<ConnectivityManager>()
    private val networkCapabilities = mock<NetworkCapabilities>()
    private val repositoryImpl = WeatherRepositoryImpl(remoteWeatherDataSource,localWeatherDataSource, dataStore, context)
    private lateinit var  expectedWeather : WeatherDto
    private lateinit var  expectedLocation : LocationDto
    private lateinit var  expectedLocationWithWeather : LocationWithWeatherDataDto
    private val cityName = "Seattle"

    @Before
    fun setUp() {

        expectedWeather =  WeatherDto(
            cityName = cityName,
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
                    timestamp = 1637094000,
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
        expectedLocation = LocationDto(cityName, "US")
        expectedLocationWithWeather = LocationWithWeatherDataDto(expectedLocation, expectedWeather)

    }


//    @ExperimentalCoroutinesApi
//    @Test
//    fun testGetWeatherWithCity() = runTest {
//        whenever(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManager)
//        whenever(connectivityManager.activeNetwork).thenReturn(mock())
//        whenever(connectivityManager.getNetworkCapabilities(any())).thenReturn(networkCapabilities)
//        whenever(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(true)
//        whenever(remoteWeatherDataSource.getWeather(expectedWeather.cityName)).thenReturn(expectedLocationWithWeather)
//        val result = repositoryImpl.getWeather(expectedWeather.cityName)
//        assertEquals(expectedWeather, result.weather)
//    }

//    @ExperimentalCoroutinesApi
//    @Test
//    fun testGetWeatherWithCoordinates() = runTest {
//        whenever(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManager)
//        whenever(connectivityManager.activeNetwork).thenReturn(mock())
//        whenever(connectivityManager.getNetworkCapabilities(any())).thenReturn(networkCapabilities)
//        whenever(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(true)
//        whenever(remoteWeatherDataSource.getWeather(expectedWeather.latitude, expectedWeather.longitude)).thenReturn(flowOf(expectedLocationWithWeather))
//        val result = repositoryImpl.getWeather(expectedWeather.latitude, expectedWeather.longitude).single()
//        assertEquals(expectedWeather, result.weather)
//    }


//    @ExperimentalCoroutinesApi
//    @Test
//    fun testGetSearchedWeather() = runTest {
//        whenever(remoteWeatherDataSource.getWeather(expectedWeather.cityName)).thenReturn(flowOf(expectedLocationWithWeather))
//        val result = repositoryImpl.getSearchedWeather(expectedWeather.cityName).single()
//        assertEquals(expectedWeather, result.weather)
//
//    }

    }
