package app.vibecast.data.remote.network.weather

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import app.vibecast.data.data_repository.data_source.remote.RemoteWeatherDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

class FetchWeatherDataWorker @Inject constructor(
    appContext: Context,
    workerParameters: WorkerParameters,
    private val remoteWeatherDataSource: RemoteWeatherDataSource) : Worker(appContext, workerParameters) {
    override fun doWork(): Result {
        TODO("Not yet implemented")
    }

    //    override fun doWork(): Result {
//        return try {
//            //TODO figure out how to provide name for city to this worker
//            val cityName = "stub"
//
//            val weatherData = remoteWeatherDataSource.getWeather(cityName)
//            if (weatherData != null){
//                //TODO insert weather data into db
//                Result.Success()
//            }
//            else {
//                Result.failure()
//            }
//        } catch (e: Exception){
//            Result.retry()
//        }
//    }
}