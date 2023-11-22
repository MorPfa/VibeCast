package app.vibecast.presentation

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