package app.vibecast.data.data_repository.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import app.vibecast.data.TAGS.WEATHER_ERROR
import app.vibecast.data.data_repository.data_source.local.LocalWeatherDataSource
import app.vibecast.data.data_repository.data_source.remote.RemoteWeatherDataSource
import app.vibecast.domain.entity.LocationDto
import app.vibecast.domain.entity.LocationWithWeatherDataDto
import app.vibecast.domain.entity.WeatherDto
import app.vibecast.domain.repository.DataStoreRepository
import app.vibecast.domain.repository.WeatherRepository
import app.vibecast.presentation.TAG
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class WeatherRepositoryImpl @Inject constructor(
    private val remoteWeatherDataSource: RemoteWeatherDataSource,
    private val localWeatherDataSource: LocalWeatherDataSource,
    private val dataStoreRepository: DataStoreRepository,
    @ApplicationContext private val appContext: Context
) : WeatherRepository{

    /**
     *  Gets weather data and location data from remote datasource based on search query
     */
    override fun getSearchedWeather(cityName: String): Flow<LocationWithWeatherDataDto> = flow{
        try{
            remoteWeatherDataSource.getWeather(cityName).collect{
                emit(it)
            }
        }
        catch (e : Exception){
            Log.e(WEATHER_ERROR, "Error fetching weather data for $cityName", e)
            throw e
        }

    }.flowOn(Dispatchers.IO)

    /**
     *  Attempts to get weather data and location data for saved location from database.
     *  If data is older than 20 minutes or the preference for weather units
     *  has changed since data was last cached, DataOutdatedException is thrown
     *  and data is fetched from server
     */

    override fun getWeather(cityName: String): Flow<LocationWithWeatherDataDto> = flow {
        try {
            val localWeatherFlow = localWeatherDataSource.getWeather(cityName)
            localWeatherFlow.collect { weatherData ->
                val timestamp = weatherData.dataTimestamp
                if (
                    (isDataOutdated(timestamp) && isInternetAvailable(appContext))
                    ||
                    (isWrongUnit(weatherData.unit) && isInternetAvailable(appContext))
                )
                {
                    throw DataOutdatedException("Timestamp: $timestamp current time: ${System.currentTimeMillis()}")
                }
                else {
                    Log.d(TAG, "local city")
                    val output = LocationWithWeatherDataDto(LocationDto(weatherData.cityName, weatherData.country), weatherData)
                    emit(output)
                }

            }
        }
        catch (e : Exception){
            Log.d(TAG, "remote city ")
            Log.d(TAG, "$e")
            remoteWeatherDataSource.getWeather(cityName).map { data ->
                data.weather.country = data.location.country
                data.weather.cityName = data.location.cityName
                data
            }.onEach {  localWeatherDataSource.addWeather(it.weather) }
                .collect {
                    emit(it)
                }
        }
    }.flowOn(Dispatchers.IO)


    /**
     *  Attempts to get weather data and location data for current location from database.
     *  If data is older than 20 minutes or the preference for weather units
     *  has changed since data was last cached, DataOutdatedException is thrown
     *  and data is fetched from server
     */
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
                        val localWeatherFlow = localWeatherDataSource.getWeather(cityName)
                        localWeatherFlow.collect { weatherData ->
                            val timestamp = weatherData.dataTimestamp
                            if (
                                (isDataOutdated(timestamp) && isInternetAvailable(appContext))
                                ||
                                (isWrongUnit(weatherData.unit) && isInternetAvailable(appContext))
                                )
                            {
                                throw DataOutdatedException("Timestamp: $timestamp current time: ${System.currentTimeMillis()}")
                            }
                            else {
                                Log.d(TAG, "local coordinates")
                                val output = LocationWithWeatherDataDto(LocationDto(weatherData.cityName, weatherData.country), weatherData)
                                emit(output)
                            }
                        }
                    }
                    catch (e : Exception){
                        Log.d(TAG, "remote coordinates")
                        Log.d(TAG, "$e")
                        remoteWeatherDataSource.getWeather(lat, lon)
                            .map { weather ->
                                weather.weather.country = data.countryName
                                weather
                            }
                            .onEach { localWeatherDataSource.addWeather(it.weather) }
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
        val outdatedThreshold = 20 * 60

        val currentTimestampInSeconds = currentTimestamp / 1000
        val savedTimestampInSeconds = savedTimestamp / 1000


        val difference = currentTimestampInSeconds - savedTimestampInSeconds

        Log.d(TAG, "Current Timestamp: $currentTimestampInSeconds")
        Log.d(TAG, "Saved Timestamp: $savedTimestampInSeconds")
        Log.d(TAG, "Difference: $difference")

        return difference > outdatedThreshold
    }

    private suspend fun isWrongUnit(unit : Unit?) : Boolean{
        val previousUnit = dataStoreRepository.getUnit()
        return unit?.name != previousUnit?.name
    }



    class DataOutdatedException(message: String) : Exception(message)


}
