package app.vibecast.domain

import app.vibecast.domain.repository.LocationRepository
import app.vibecast.domain.usecase.GetSavedWeatherUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetSavedWeatherUseCaseTest {

    private val locationRepository = mock<LocationRepository>()
    private val useCase = GetSavedWeatherUseCase(mock(),locationRepository)


    @ExperimentalCoroutinesApi
    @Test
    fun testProcess()= runTest {
        val request = GetSavedWeatherUseCase.Request(0)
        val fakeWeather = CreateFakeWeather().createFakeWeather()
        whenever(locationRepository.getLocationWeather(request.index)).thenReturn(flowOf(fakeWeather))
        val response = useCase.process(request).first()
        assertEquals(GetSavedWeatherUseCase.Response(fakeWeather),response)
    }
}