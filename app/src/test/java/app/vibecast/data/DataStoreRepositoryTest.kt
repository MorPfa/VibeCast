package app.vibecast.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import app.vibecast.data.data_repository.repository.DataStoreRepositoryImpl
import app.vibecast.domain.repository.weather.Unit
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineExceptionHandler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.createTestCoroutineScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DataStoreRepositoryImplTest {

    private val testDispatcher = TestCoroutineDispatcher()
    @ExperimentalCoroutinesApi
    private val testScope =
        createTestCoroutineScope(TestCoroutineDispatcher() + TestCoroutineExceptionHandler() + testDispatcher)

    private val context = mockk<Context>()
    private val dataStore = mockk<DataStore<Preferences>>()
    private val dataStoreRepository = DataStoreRepositoryImpl(context)

    private lateinit var unit: Unit

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        unit = Unit.METRIC
        Dispatchers.setMain(testDispatcher)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testPutUnit() = testScope.runTest {
        coEvery { dataStore.edit(any()) } coAnswers { invocation ->
            val block: suspend (MutablePreferences) -> Unit = invocation.invocation.args[0] as suspend (MutablePreferences) -> Unit
            val mockPreferences = mockk<MutablePreferences>(relaxed = true)
            block.invoke(mockPreferences)
            mockPreferences
        }

        dataStoreRepository.savePreferences(unit)
        testScope.advanceUntilIdle()
        coVerify { dataStore.edit(any()) }

    }

    @ExperimentalCoroutinesApi
    @Test
    fun testGetUnit() = testScope.runTest {
        val unitString = "METRIC"
        val expectedUnit = Unit.METRIC

        every { dataStore.data } returns flowOf(
            mockk {
                every { this[any()] }
            }
        )

        val result = dataStoreRepository.getPreferences()

        assertEquals(expectedUnit, result)

        // Verify that the mock was called
        verify { dataStore.data }
    }



    @ExperimentalCoroutinesApi
    @Test
    fun testClearUnit() = testScope.runTest {
        coEvery { dataStore.edit(any()) } coAnswers { invocation ->
            val block: suspend (MutablePreferences) -> Unit = invocation.invocation.args[0] as suspend (MutablePreferences) -> Unit
            val mockPreferences = mockk<MutablePreferences>(relaxed = true)
            block.invoke(mockPreferences)
            mockPreferences
        }

        dataStoreRepository.clearPreferences()
        coVerify { dataStore.edit(any()) }
        testScope.advanceUntilIdle()
    }

}
