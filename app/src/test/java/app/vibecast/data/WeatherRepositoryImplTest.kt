package app.vibecast.data

import app.vibecast.data.data_repository.data_source.local.LocalWeatherDataSource
import app.vibecast.data.data_repository.data_source.remote.RemoteWeatherDataSource
import app.vibecast.data.data_repository.repository.WeatherRepositoryImpl
import app.vibecast.domain.CreateFakeWeather
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class WeatherRepositoryImplTest {

    private val remoteWeatherDataSource = mock<RemoteWeatherDataSource>()
    private val localWeatherDataSource = mock<LocalWeatherDataSource>()
    private val repositoryImpl = WeatherRepositoryImpl(remoteWeatherDataSource,localWeatherDataSource)


    @ExperimentalCoroutinesApi
    @Test
    fun testGetWeather() = runTest {
        val cityName = "London"
        val weather = CreateFakeWeather().createFakeWeather()
        whenever(remoteWeatherDataSource.getWeather(cityName)).thenReturn(flowOf(weather))
        val result = repositoryImpl.getWeather(cityName).first()
        assertEquals(weather, result)
        verify(localWeatherDataSource).addWeather(cityName)
    }
}