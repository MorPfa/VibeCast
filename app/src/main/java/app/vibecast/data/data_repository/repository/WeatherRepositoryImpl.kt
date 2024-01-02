package app.vibecast.data.data_repository.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import app.vibecast.data.data_repository.data_source.local.LocalWeatherDataSource
import app.vibecast.data.data_repository.data_source.remote.RemoteWeatherDataSource
import app.vibecast.data.remote.network.weather.CoordinateApiModel
import app.vibecast.domain.entity.LocationWithWeatherDataDto
import app.vibecast.domain.entity.WeatherDto
import app.vibecast.domain.repository.WeatherRepository
import app.vibecast.presentation.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class WeatherRepositoryImpl @Inject constructor(
    private val remoteWeatherDataSource: RemoteWeatherDataSource,
    private val localWeatherDataSource: LocalWeatherDataSource,
) : WeatherRepository{


    override fun getCoordinates(cityName: String): Flow<CoordinateApiModel> = flow {
        remoteWeatherDataSource.getCoordinates(cityName)
            .onCompletion { cause ->
                if (cause != null && cause !is CancellationException) {
                    Log.e(TAG, "Error during flow completion for getCoordinates: $cause in Repository")
                }
            }
            .collect { emit(it) }
    }.flowOn(Dispatchers.IO)


    override fun getWeather(cityName: String): Flow<LocationWithWeatherDataDto> = flow {
        remoteWeatherDataSource.getWeather(cityName)
            .onCompletion { cause ->
                if (cause != null && cause !is CancellationException) {
                    Log.e(TAG, "Error during flow completion for getWeather: $cause in Repository")
                }
            }
            .collect { emit(it) }
    }.flowOn(Dispatchers.IO)



    override fun getWeather(lat: Double, lon: Double): Flow<LocationWithWeatherDataDto> = flow {
        remoteWeatherDataSource.getCity(lat, lon)
            .onCompletion { cause ->
                if (cause != null && cause !is CancellationException) {
                    Log.e(TAG, "Error during flow completion for getWeather with lat=$lat, lon=$lon: $cause in Repository")
                }
            }
            .collect { data ->
                val cityName = data.cityName
                if (cityName.isNotBlank()) {
                    try {
                        val localWeatherFlow = localWeatherDataSource.getLocationWithWeather(cityName)
                        localWeatherFlow.collect { weatherData ->
                            if (weatherData != null) {
                                // Location found in the local database, emit the data
                                emit(weatherData)
                            }
                    }
                    }
                    catch (e : Exception){
                            remoteWeatherDataSource.getWeather(lat, lon)
                                .collect {
                                    it.location.cityName = data.cityName
                                    it.location.country = data.countryName
                                    emit(it)

                        }
                    }
                }
            }
    }.flowOn(Dispatchers.IO)


    override fun refreshWeather(cityName: String): Flow<WeatherDto> = flow {
        remoteWeatherDataSource.getWeather(cityName)
            .onEach {
                localWeatherDataSource.addWeather(it.weather)
                emit(it.weather)
            }
            .onCompletion { cause ->
                if (cause != null && cause !is CancellationException) {
                    Log.e(TAG, "Error during flow completion for refreshWeather with $cityName: $cause in Repository")
                }
            }
    }.flowOn(Dispatchers.IO)

    override fun refreshWeather(lat: Double, lon: Double): Flow<WeatherDto> = flow {
        remoteWeatherDataSource.getWeather(lat, lon)
            .onEach {
                localWeatherDataSource.addWeather(it.weather)
                emit(it.weather)
            }
            .onCompletion { cause ->
                if (cause != null && cause !is CancellationException) {
                    Log.e(TAG, "Error during flow completion for refreshWeather with lat=$lat, lon=$lon: $cause in Repository")
                }
            }
    }.flowOn(Dispatchers.IO)



    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

}
