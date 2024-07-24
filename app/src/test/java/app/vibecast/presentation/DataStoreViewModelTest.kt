//package app.vibecast.presentation
//
//import app.vibecast.domain.repository.weather.Unit
//import app.vibecast.domain.repository.weather.UnitPreferenceRepository
//import app.vibecast.presentation.screens.settings_screen.PreferencesViewModel
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.flow.single
//import kotlinx.coroutines.test.runTest
//import kotlinx.coroutines.test.setMain
//import org.junit.Assert.assertEquals
//import org.junit.Before
//import org.junit.Test
//import org.mockito.Mockito.mock
//import org.mockito.kotlin.verify
//import org.mockito.kotlin.whenever
//
//
//class DataStoreViewModelTest {
//
//    private val dataStoreRepository = mock<UnitPreferenceRepository>()
//    private val viewModel = PreferencesViewModel(dataStoreRepository)
//    private lateinit var unit : Unit
//
//    @ExperimentalCoroutinesApi
//    @Before
//    fun setup() {
//         unit = Unit.METRIC
//         Dispatchers.setMain(Dispatchers.Unconfined)
//    }
//
//    @Test
//    fun testStoreUnit() = runTest {
//        whenever(dataStoreRepository.getPreference()).thenReturn(unit)
//        viewModel.storeUnit(unit)
//        val result = viewModel.getUnit().single()
//        assertEquals(unit, result)
//        verify(dataStoreRepository).savePreference(unit)
//
//    }
//
//    @Test
//    fun testGetUnit(): kotlin.Unit = runTest {
//        whenever(dataStoreRepository.getPreference()).thenReturn(unit)
//        val result = viewModel.getUnit().single()
//        assertEquals(unit, result)
//        verify(dataStoreRepository).getPreference()
//    }
//
//    @Test
//    fun testClearUnit() = runTest {
//        viewModel.clearUnit()
//        val result = viewModel.getUnit().single()
//        assertEquals(result, null )
//        verify(dataStoreRepository).clearPreference()
//
//    }
//}
