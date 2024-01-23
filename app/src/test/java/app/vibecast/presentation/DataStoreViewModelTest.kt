package app.vibecast.presentation

import app.vibecast.data.data_repository.repository.Unit
import app.vibecast.domain.repository.DataStoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever


class DataStoreViewModelTest {

    private val dataStoreRepository = mock<DataStoreRepository>()
    private val viewModel = DataStoreViewModel(dataStoreRepository)
    private lateinit var unit : Unit

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
         unit = Unit.METRIC
         Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @Test
    fun testStoreUnit() = runTest {
        whenever(dataStoreRepository.getUnit()).thenReturn(unit)
        viewModel.storeUnit(unit)
        val result = viewModel.getUnit().single()
        assertEquals(unit, result)
        verify(dataStoreRepository).putUnit(unit)

    }

    @Test
    fun testGetUnit(): kotlin.Unit = runTest {
        whenever(dataStoreRepository.getUnit()).thenReturn(unit)
        val result = viewModel.getUnit().single()
        assertEquals(unit, result)
        verify(dataStoreRepository).getUnit()
    }

    @Test
    fun testClearUnit() = runTest {
        viewModel.clearUnit()
        val result = viewModel.getUnit().single()
        assertEquals(result, null )
        verify(dataStoreRepository).clearUnit()

    }
}
