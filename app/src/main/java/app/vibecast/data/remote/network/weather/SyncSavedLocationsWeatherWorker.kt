package app.vibecast.data.remote.network.weather

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import app.vibecast.domain.entity.Location
import app.vibecast.domain.repository.LocationRepository
import app.vibecast.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class SyncSavedLocationsWeatherWorker @Inject constructor(
    appContext: Context,
    workerParameters: WorkerParameters,
    private val weatherRepository: WeatherRepository,
    private val locationRepository: LocationRepository
) : Worker(appContext, workerParameters) {


    override fun doWork(): Result {
        return try {
            val savedLocations: Flow<List<Location>> = locationRepository.getLocations()

            runBlocking {
                savedLocations.collect { locations ->
                    for (location in locations) {
                        val cityName = location.cityName
                        weatherRepository.refreshWeather(cityName)


                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}

