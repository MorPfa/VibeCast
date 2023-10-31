package app.vibecast.domain

import app.vibecast.domain.repository.WeatherRepository
import app.vibecast.domain.usecase.GetWeatherUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetWeatherUseCaseTest {

    private val weatherRepository = mock<WeatherRepository>()
    private val useCase = GetWeatherUseCase(mock(),weatherRepository)


    @ExperimentalCoroutinesApi
    @Test
    fun testProcess()= runTest {
        val request = GetWeatherUseCase.Request("London")
        val fakeWeather = CreateFakeWeather().createFakeWeather()
        whenever(weatherRepository.getWeather(request.weatherDataCityName)).thenReturn(flowOf(fakeWeather))
        val response = useCase.process(request).first()
        assertEquals(GetWeatherUseCase.Response(fakeWeather),response)
    }
}