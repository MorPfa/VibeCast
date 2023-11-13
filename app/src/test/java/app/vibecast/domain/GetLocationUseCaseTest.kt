package app.vibecast.domain

import app.vibecast.domain.entity.Location
import app.vibecast.domain.repository.LocationRepository
import app.vibecast.domain.usecase.GetLocationUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetLocationUseCaseTest {

    private val locationRepository = mock<LocationRepository>()
    private val useCase = GetLocationUseCase(mock(),locationRepository)


    @ExperimentalCoroutinesApi
    @Test
    fun testProcess()= runTest {
        val request = GetLocationUseCase.Request("London")
        val location = Location("London", 1)
        whenever(locationRepository.getLocation(request.cityName)).thenReturn(flowOf(location))
        val response = useCase.process(request).first()
        Assert.assertEquals(GetLocationUseCase.Response(location),response)
    }
}