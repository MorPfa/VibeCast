package app.vibecast.presentation

import app.vibecast.domain.entity.Result
import app.vibecast.presentation.weather.WeatherViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class WeatherViewModelTest {



//
//    @ExperimentalCoroutinesApi
//    private val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()
//    private val useCase = mock<GetCurrentWeatherUseCase>()
//    private val converter = mock<WeatherConverter>()
//    @ExperimentalCoroutinesApi
//    private val viewModel = WeatherViewModel(useCase, converter)
//
//    @ExperimentalCoroutinesApi
//    @Before
//    fun setUp() {
//        Dispatchers.setMain(testDispatcher)
//    }
//
//    @ExperimentalCoroutinesApi
//    @After
//    fun tearDown() {
//        Dispatchers.resetMain()
//        testDispatcher.cleanupTestCoroutines()
//    }
//
//    @ExperimentalCoroutinesApi
//    @Test
//    fun testLoadPost() = runTest {
//        assertEquals(UiState.Loading, viewModel.weatherFlow.value)
//        val cityName = "London"
//        val uiState = mock<UiState<WeatherModel>>()
//        val result = mock<Result<GetCurrentWeatherUseCase.Response>>()
//        whenever(useCase.execute(GetCurrentWeatherUseCase.Request(cityName))).thenReturn(flowOf(result))
//        whenever(converter.convert(result)).thenReturn(uiState)
//        viewModel.loadWeather(cityName)
//        assertEquals(uiState, viewModel.weatherFlow.value)
//    }
}