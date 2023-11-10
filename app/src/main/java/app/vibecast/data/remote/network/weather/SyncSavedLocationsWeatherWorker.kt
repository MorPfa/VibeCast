package app.vibecast.data.remote.network.weather

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.vibecast.domain.repository.LocationRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncSavedLocationsWeatherWorker @Inject constructor(
    appContext: Context,
    workerParameters: WorkerParameters,
    private val locationRepository: LocationRepository
) : CoroutineWorker(appContext, workerParameters) {


    override suspend fun doWork(): Result {
        return try {
            locationRepository.refreshLocationWeather()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    fun enqueueWeatherSyncWork(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<SyncCurrentLocationWeatherWorker>(
            repeatInterval = 10, // Set the desired interval
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork("weatherSyncWork", ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest)
    }

}

