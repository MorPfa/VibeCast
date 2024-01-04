package app.vibecast.data.data_repository.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import app.vibecast.data.data_repository.data_source.local.LocalWeatherDataSource
import app.vibecast.data.data_repository.data_source.remote.RemoteWeatherDataSource
import app.vibecast.data.remote.network.weather.CoordinateApiModel
import app.vibecast.domain.entity.LocationDto
import app.vibecast.domain.entity.LocationWithWeatherDataDto
import app.vibecast.domain.entity.WeatherDto
import app.vibecast.domain.repository.WeatherRepository
import app.vibecast.presentation.TAG
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val appContext: Context
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
        try {
            Log.d(TAG, "local city")
            val localWeatherFlow = localWeatherDataSource.getLocationWithWeather(cityName)
            localWeatherFlow.collect { weatherData ->
                val timestamp = weatherData.weather.dataTimestamp
                if (isDataOutdated(timestamp) && isInternetAvailable(appContext)){
                    throw DataOutdatedException("Timestamp: $timestamp current time: ${System.currentTimeMillis()}")
                }
                else {
                    Log.d(TAG, "local city")
                    emit(weatherData)
                }
                emit(weatherData)
            }
        }
        catch (e : Exception){
            Log.d(TAG, "remote city ")
            Log.d(TAG, "$e")
            remoteWeatherDataSource.getWeather(cityName).onEach {  localWeatherDataSource.addLocationWithWeather(
                LocationWithWeatherDataDto(LocationDto(it.location.cityName, it.location.country),it.weather)
            ) }
                .collect {
                    emit(it)

                }
        }
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
                            val timestamp = weatherData.weather.dataTimestamp
                            if (isDataOutdated(timestamp) && isInternetAvailable(appContext)){
                                throw DataOutdatedException("Timestamp: $timestamp current time: ${System.currentTimeMillis()}")
                            }
                            else {
                                Log.d(TAG, "local coordinates")
                                emit(weatherData)
                            }
                        }
                    }
                    catch (e : Exception){
                        Log.d(TAG, "remote coordinates")
                        Log.d(TAG, "$e")
                            remoteWeatherDataSource.getWeather(lat, lon).onEach {  localWeatherDataSource.addLocationWithWeather(
                                LocationWithWeatherDataDto(LocationDto(data.cityName, data.countryName),it.weather)
                            ) }
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


    private fun isDataOutdated(savedTimestamp: Long): Boolean {
        val currentTimestamp = System.currentTimeMillis()
        val outdatedThreshold = 20 * 60 * 1000

        // Ensure both timestamps have the same number of digits (10 digits)
        val adjustedCurrentTimestamp = currentTimestamp.toString().take(10).padStart(10, '0')
        val adjustedSavedTimestamp = savedTimestamp.toString().take(10).padStart(10, '0')

        val difference = adjustedCurrentTimestamp.toLong() - adjustedSavedTimestamp.toLong()

        Log.d(TAG, "Current Timestamp: $adjustedCurrentTimestamp")
        Log.d(TAG, "Saved Timestamp: $adjustedSavedTimestamp")
        Log.d(TAG, "Difference: $difference")

        return difference > outdatedThreshold
    }





    class DataOutdatedException(message: String) : Exception(message)


}
