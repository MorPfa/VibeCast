package app.vibecast.domain.repository.weather

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import app.vibecast.data.local_data.data_source.weather.LocalWeatherDataSource
import app.vibecast.data.remote_data.data_source.weather.RemoteWeatherDataSource
import app.vibecast.domain.model.LocationDto
import app.vibecast.domain.model.LocationWithWeatherDataDto
import app.vibecast.domain.model.WeatherDto
import app.vibecast.domain.util.TAGS.WEATHER_ERROR
import app.vibecast.presentation.TAG
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException


/**
 * Implementation of [WeatherRepository]
 *
 * Methods:
 * - [getSearchedWeather] Gets weather data and location data from remote datasource based on search query
 * - [getWeather] Attempts to get weather and location data for saved location from database.
 *                If data is older than 20 minutes or the preference for weather units has changed since
 *                data was last cached, DataOutdatedException is thrown and data is fetched from server
 * - [getWeather] Same as [getWeather] but based on coordinates for users current location
 * - [isDataOutdated] Checks timestamp on saved database and determines if it is out of date
 * - [isWrongUnit] Checks if preference for weather data units has changed since data has last been saved to database
 */
class WeatherRepositoryImpl @Inject constructor(
    private val remoteWeatherDataSource: RemoteWeatherDataSource,
    private val localWeatherDataSource: LocalWeatherDataSource,
    private val dataStoreRepository: UnitPreferenceRepository,
    @ApplicationContext private val appContext: Context,
) : WeatherRepository {


    override val currentWeather = MutableStateFlow<String?>(null)

    override fun getSearchedWeather(cityName: String): Flow<LocationWithWeatherDataDto?> = flow {
        try {
            val test = remoteWeatherDataSource.getSearchedWeather(cityName).firstOrNull()
            val currentWeatherCondition =
                test?.weather?.currentWeather?.weatherConditions?.get(0)?.mainDescription
            currentWeather.emit(currentWeatherCondition)
            emit(test)

        } catch (e: Exception) {
            Timber.tag(WEATHER_ERROR).e(e, "Error fetching weather data for $cityName")
            throw e
        }

    }.flowOn(Dispatchers.IO)


    override fun getWeather(cityName: String): Flow<LocationWithWeatherDataDto> = flow {
        try {
            val localWeatherFlow = localWeatherDataSource.getWeather(cityName)
            localWeatherFlow.collect { weatherData ->
                val timestamp = weatherData.dataTimestamp
                if (
                    (isDataOutdated(timestamp) && isInternetAvailable(appContext))
                    ||
                    (isWrongUnit(weatherData.unit) && isInternetAvailable(appContext))
                ) {
                    throw DataOutdatedException("Timestamp: $timestamp current time: ${System.currentTimeMillis()}")
                } else {
                    Timber.tag(TAG).d("local city")
                    val output = LocationWithWeatherDataDto(
                        LocationDto(
                            weatherData.cityName,
                            weatherData.country
                        ), weatherData
                    )
                    val currentWeatherCondition =
                        output.weather.currentWeather?.weatherConditions?.get(0)?.mainDescription
                    currentWeather.emit(currentWeatherCondition)
                    emit(output)
                }

            }
        } catch (e: Exception) {
            Timber.tag(TAG).d("remote city ")
            Timber.tag(TAG).d(e)
            remoteWeatherDataSource.getWeather(cityName).map { data ->
                data.weather.country = data.location.country
                data.weather.cityName = data.location.city
                data
            }.onEach { localWeatherDataSource.addWeather(it.weather) }
                .collect {
                    val currentWeatherCondition =
                        it.weather.currentWeather?.weatherConditions?.get(0)?.mainDescription
                    currentWeather.emit(currentWeatherCondition)
                    emit(it)
                }
        }
    }.flowOn(Dispatchers.IO)


    override fun getWeather(lat: Double, lon: Double): Flow<LocationWithWeatherDataDto> = flow {
        remoteWeatherDataSource.getCity(lat, lon)
            .onCompletion { cause ->
                if (cause != null && cause !is CancellationException) {
                    Timber.tag(TAG)
                        .e("Error during flow completion for getWeather with lat= $lat  $lon   $cause  in Repository")
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
                            ) {
                                throw DataOutdatedException("Timestamp: $timestamp current time: ${System.currentTimeMillis()}")
                            } else {
                                Timber.tag(TAG).d("local coordinates")
                                val output = LocationWithWeatherDataDto(
                                    LocationDto(
                                        weatherData.cityName,
                                        weatherData.country
                                    ), weatherData
                                )
                                val currentWeatherCondition =
                                    output.weather.currentWeather?.weatherConditions?.get(0)?.mainDescription
                                currentWeather.emit(currentWeatherCondition)
                                emit(output)
                            }
                        }
                    } catch (e: Exception) {
                        Timber.tag(TAG).d("remote coordinates")
                        Timber.tag(TAG).d(e)
                        remoteWeatherDataSource.getWeather(lat, lon)
                            .map { weather ->
                                weather.weather.country = data.countryName
                                weather
                            }
                            .onEach { localWeatherDataSource.addWeather(it.weather) }
                            .collect {
                                it.location.city = data.cityName
                                it.location.country = data.countryName
                                val currentWeatherCondition =
                                    it.weather.currentWeather?.weatherConditions?.get(0)?.mainDescription
                                currentWeather.emit(currentWeatherCondition)
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
                    Timber.tag(TAG)
                        .e("Error during flow completion for refreshWeather with  $cityName  $cause  in Repository")
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
                    Timber.tag(TAG)
                        .e("Error during flow completion for getWeather with lat= $lat  $lon   $cause  in Repository")
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

        Timber.tag(TAG).d("Current Timestamp: $currentTimestampInSeconds")
        Timber.tag(TAG).d("Saved Timestamp: $savedTimestampInSeconds")
        Timber.tag(TAG).d("Difference: $difference")

        return difference > outdatedThreshold
    }

    private suspend fun isWrongUnit(unit: Unit?): Boolean {
        val previousUnit = dataStoreRepository.getPreference()
        return unit?.name != previousUnit.name
    }


    class DataOutdatedException(message: String) : Exception(message)


}
