package app.vibecast.domain

import app.vibecast.domain.repository.WeatherRepository
import app.vibecast.domain.usecase.GetCurrentWeatherUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetCurrentWeatherUseCaseTest {

    private val weatherRepository = mock<WeatherRepository>()
    private val useCase = GetCurrentWeatherUseCase(mock(),weatherRepository)


    @ExperimentalCoroutinesApi
    @Test
    fun testProcess()= runTest {
        val request = GetCurrentWeatherUseCase.Request("London")
        val fakeWeather = CreateFakeWeather().createFakeWeather()
        whenever(weatherRepository.getWeather(request.weatherDataCityName)).thenReturn(flowOf(fakeWeather))
        val response = useCase.process(request).first()
        assertEquals(GetCurrentWeatherUseCase.Response(fakeWeather),response)
    }
}