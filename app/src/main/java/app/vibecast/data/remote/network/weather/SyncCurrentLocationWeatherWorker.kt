package app.vibecast.data.remote.network.weather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.vibecast.BuildConfig
import app.vibecast.domain.repository.WeatherRepository
import com.google.android.gms.location.LocationServices
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncCurrentLocationWeatherWorker @Inject constructor(
    private val appContext: Context,
    workerParameters: WorkerParameters,
    private val weatherRepository: WeatherRepository,
    private val weatherService: WeatherService
) : CoroutineWorker(appContext, workerParameters) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)

    override suspend fun doWork(): Result {
        return try {
            if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // If the permission is not granted, return a failure result or handle it accordingly
                Result.failure()
            } else {
                // Permission is granted; proceed with location retrieval
                val locationResult = fusedLocationClient.lastLocation.result
                if (locationResult != null) {
                    val latitude = locationResult.latitude
                    val longitude = locationResult.longitude
                    val weatherData = weatherService.getWeather(latitude, longitude, BuildConfig.OWM_KEY)
                    weatherRepository.refreshWeather(weatherData.cityName)
                }
                Result.success()
            }
        } catch (e: Exception) {
            // Handle exceptions (e.g., location not available, permission denied)
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