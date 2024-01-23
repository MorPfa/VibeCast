//package app.vibecast.data
//
//import android.content.Context
//import androidx.test.core.app.ApplicationProvider
//import androidx.work.*
//import app.vibecast.data.remote.network.weather.SyncSavedLocationsWeatherWorker
//import app.vibecast.domain.repository.LocationRepository
//import kotlinx.coroutines.runBlocking
//import org.junit.Before
//import org.junit.Test
//import org.mockito.kotlin.mock
//import org.mockito.kotlin.verify
//import java.util.concurrent.TimeUnit
//
//class SyncSavedLocationsWeatherWorkerTest {
//
//    private lateinit var context: Context
//    private lateinit var locationRepository: LocationRepository
//    private lateinit var worker: SyncSavedLocationsWeatherWorker
//
//    @Before
//    fun setUp() {
//        context = ApplicationProvider.getApplicationContext()
//        locationRepository = mock()
//        val workerParameters = WorkerParameters
//            .setApplicationContext(context)
//            .setWorkerExecutor(TestWorkerExecutor())
//            .build()
//        worker = SyncSavedLocationsWeatherWorker(context, workerParameters, locationRepository)
//    }
//
//    @Test
//    fun `doWork success`() {
//        runBlocking {
//            // Mock the refreshLocationWeather function to succeed
//            worker.doWork()
//
//            // Verify that refreshLocationWeather was called
//            verify(locationRepository).refreshLocationWeather()
//        }
//    }
//
//    @Test
//    fun `doWork failure`() {
//        runBlocking {
//            // Mock the refreshLocationWeather function to throw an exception
//            locationRepository = mock { onBlocking { refreshLocationWeather() }.thenThrow(Exception()) }
//            val worker = SyncSavedLocationsWeatherWorker(context, workerParameters, locationRepository)
//
//            // Ensure that Result.failure() is returned on exception
//            val result = worker.doWork()
//            assert(result is Result.Failure)
//        }
//    }
//
//    @Test
//    fun `enqueueWeatherSyncWork enqueues periodic work`() {
//        // Create constraints for the periodic work
//        val constraints = Constraints.Builder()
//            .setRequiredNetworkType(NetworkType.CONNECTED)
//            .build()
//
//        // Create a periodic work request with a repeat interval of 10 minutes
//        val periodicWorkRequest = PeriodicWorkRequestBuilder<SyncSavedLocationsWeatherWorker>(
//            repeatInterval = 10,
//            repeatIntervalTimeUnit = TimeUnit.MINUTES
//        )
//            .setConstraints(constraints)
//            .build()
//
//        // Mock WorkManager and enqueueUniquePeriodicWork function
//        val workManager = mock<WorkManager> {
//            on { enqueueUniquePeriodicWork("weatherSyncWork", ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest) }
//                .thenReturn(mock())
//        }
//
//        // Set the mocked WorkManager in the worker
//        worker.setWorkManager(workManager)
//
//        // Call the enqueueWeatherSyncWork function
//        worker.enqueueWeatherSyncWork(context)
//
//        // Verify that enqueueUniquePeriodicWork was called with the correct parameters
//        verify(workManager).enqueueUniquePeriodicWork(
//            "weatherSyncWork",
//            ExistingPeriodicWorkPolicy.KEEP,
//            periodicWorkRequest
//        )
//    }
//}
//
//class TestWorkerExecutor : WorkerExecutor() {
//    override fun execute(runnable: Runnable) {
//        runnable.run()
//    }
//}
