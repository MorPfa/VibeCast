package app.vibecast.data

import app.vibecast.BuildConfig
import app.vibecast.data.remote.network.weather.CoordinateApiModel
import app.vibecast.data.remote.network.weather.WeatherService
import app.vibecast.data.remote.source.RemoteWeatherDataSourceImpl
import app.vibecast.domain.CreateFakeWeather
import app.vibecast.domain.entity.UseCaseException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlinx.coroutines.flow.collect


class RemoteWeatherDataSourceImplTest {

    private val weatherService = mock<WeatherService>()
    private val weatherDataSource = RemoteWeatherDataSourceImpl(weatherService)


    @ExperimentalCoroutinesApi
    @Test
    fun testGetCityCoordinates() = runTest {
        val cityName = "London"
        val remoteCoordinates = CoordinateApiModel(51.5073219,-0.1276474 )
        whenever(weatherService.getCiyCoordinates(cityName,1,BuildConfig.OWM_KEY)).thenReturn(remoteCoordinates)
        val result = weatherDataSource.getCity(cityName).first()
        assertEquals(remoteCoordinates, result)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testGetWeather() = runTest {
        val cityName = "London"
        val remoteCoordinates = CoordinateApiModel(51.5073219, -0.1276474)

        whenever(weatherService.getCiyCoordinates(cityName, 1, BuildConfig.OWM_KEY)).thenReturn(remoteCoordinates)

        val remoteWeather = CreateFakeWeatherResponse().createFakeWeatherApiModel()
        val expectedWeather = CreateFakeWeather().createFakeWeather()
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