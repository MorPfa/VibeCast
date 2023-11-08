package app.vibecast.data.remote.network.weather

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.vibecast.domain.entity.Location
import app.vibecast.domain.repository.LocationRepository
import app.vibecast.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncSavedLocationsWeatherWorker @Inject constructor(
    appContext: Context,
    workerParameters: WorkerParameters,
    private val weatherRepository: WeatherRepository,
    private val locationRepository: LocationRepository
) : CoroutineWorker(appContext, workerParameters) {


    override suspend fun doWork(): Result {
        return try {
            val savedLocations: Flow<List<Location>> = locationRepository.getLocations()
            savedLocations.collect { locations ->
                    for (location in locations) {
                        val cityName = location.cityName
                        weatherRepository.refreshWeather(cityName)
                    }
                }
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

