package app.vibecast.domain.repository.weather

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import app.vibecast.data.local_data.data_source.weather.LocalWeatherDataSource
import app.vibecast.data.remote_data.data_source.weather.RemoteWeatherDataSource
import app.vibecast.domain.model.LocationDto
import app.vibecast.domain.model.LocationWithWeatherDataDto
import app.vibecast.domain.util.Resource
import app.vibecast.presentation.TAG
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject


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


    override suspend fun getSearchedWeather(cityName: String): Resource<LocationWithWeatherDataDto> {
        return when (val weatherData = remoteWeatherDataSource.getWeather(cityName)) {
            is Resource.Success -> {
                Resource.Success(weatherData.data!!)
            }

            is Resource.Error -> {
                Resource.Error(weatherData.message)
            }
        }
    }


    override suspend fun getWeather(cityName: String): Resource<LocationWithWeatherDataDto>  {
        return try {
            when(val cachedWeatherData = localWeatherDataSource.getWeather(cityName)){
                is Resource.Success -> {
                    val timestamp = cachedWeatherData.data?.dataTimestamp!!
                    if (
                        (isDataOutdated(timestamp) && isInternetAvailable(appContext))
                        ||
                        (isWrongUnit(cachedWeatherData.data.unit) && isInternetAvailable(appContext))
                    ) {
                        when(val remoteWeatherData = remoteWeatherDataSource.getWeather(cityName)){
                            is Resource.Success -> {
                                remoteWeatherData.data?.weather?.country = cachedWeatherData.data.country
                                remoteWeatherData.data?.weather?.cityName = cachedWeatherData.data.cityName
                                localWeatherDataSource.addWeather(remoteWeatherData.data?.weather!!)
                                Resource.Success(remoteWeatherData.data)
                            }
                            is Resource.Error -> {
                                Resource.Error(remoteWeatherData.message!!)
                            }
                        }
                    } else {
                        Timber.tag("WEATHER_REFORMAT").d("local city")
                   Resource.Success(LocationWithWeatherDataDto(
                       LocationDto(
                           cachedWeatherData.data.cityName,
                           cachedWeatherData.data.country
                       ),  cachedWeatherData.data
                   ))
                    }
                }
                is Resource.Error -> {
                    when(val remoteWeatherData = remoteWeatherDataSource.getWeather(cityName)){
                        is Resource.Success -> {
                            remoteWeatherData.data?.weather?.country = remoteWeatherData.data?.location?.country!!
                            remoteWeatherData.data.weather.cityName = remoteWeatherData.data.location.country
                            localWeatherDataSource.addWeather(remoteWeatherData.data.weather)
                            Resource.Success(remoteWeatherData.data)
                        }
                        is Resource.Error -> {
                            Resource.Error(remoteWeatherData.message!!)
                        }
                    }

                }
            }

        } catch (e: Exception) {
            Timber.tag("WEATHER_REFORMAT").d("remote city ")
            Timber.tag("WEATHER_REFORMAT").d(e)
           Resource.Error(e.localizedMessage!!)
        }
    }

    override suspend fun getWeather(
        lat: Double,
        lon: Double,
    ): Resource<LocationWithWeatherDataDto> {
        return try {
            when (val geoCodedLocation = remoteWeatherDataSource.getCity(lat, lon)) {
                is Resource.Success -> {
                    val cityName = geoCodedLocation.data?.cityName!!
                    Timber.tag("WEATHER_REFORMAT").d("Cityname: $cityName")
                    when (val cachedWeatherData = localWeatherDataSource.getWeather(cityName)) {
                        is Resource.Success -> {
                            val timestamp = cachedWeatherData.data?.dataTimestamp!!
                            if (
                                (isDataOutdated(timestamp) && isInternetAvailable(appContext))
                                ||
                                (isWrongUnit(cachedWeatherData.data.unit) && isInternetAvailable(
                                    appContext
                                ))
                            ) {
                                val remoteData = remoteWeatherDataSource.getWeather(lat, lon)
                                Timber.tag("WEATHER_REFORMAT").d("remote data: ${ geoCodedLocation.data.cityName}")
                                when (remoteData) {
                                    is Resource.Success -> {
                                        remoteData.data?.weather?.country =
                                            geoCodedLocation.data.countryName
                                        remoteData.data?.weather?.cityName =
                                            geoCodedLocation.data.cityName
                                        localWeatherDataSource.addWeather(remoteData.data?.weather!!)
                                        Resource.Success(remoteData.data)
                                    }

                                    is Resource.Error -> {
                                        Resource.Error("Couldn't get weather data for coordinates ${remoteData.message}")
                                    }
                                }
                            } else {
                                Timber.tag("WEATHER_REFORMAT").d("local city: $cityName")

                                Resource.Success(
                                    LocationWithWeatherDataDto(
                                        LocationDto(
                                            cachedWeatherData.data.cityName,
                                            cachedWeatherData.data.country
                                        ), cachedWeatherData.data
                                    )
                                )

                            }
                        }

                        is Resource.Error -> {

                            when (val remoteData = remoteWeatherDataSource.getWeather(lat, lon)) {
                                is Resource.Success -> {
                                    remoteData.data?.weather?.country =
                                        geoCodedLocation.data.countryName
                                    remoteData.data?.weather?.cityName =
                                        geoCodedLocation.data.cityName
                                    Timber.tag("WEATHER_REFORMAT").d("remote City: ${ geoCodedLocation.data.cityName}")
                                    Timber.tag("WEATHER_REFORMAT").d("Country: ${ geoCodedLocation.data.countryName}")
                                    localWeatherDataSource.addWeather(remoteData.data?.weather!!)
                                    Resource.Success(remoteData.data)
                                }
                                is Resource.Error -> {
                                    Resource.Error("Couldn't get weather data for coordinates ${remoteData.message}")
                                }
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Resource.Error("Couldn't get city name for coordinates ${geoCodedLocation.message!!}")
                }
            }
        } catch(e : Exception){
            Resource.Error("Couldn't get weather data for coordinates ${e.localizedMessage}")
        }
    }



//    override fun refreshWeather(cityName: String): Flow<WeatherDto> = flow {
//        remoteWeatherDataSource.getWeather(cityName)
//            .onEach {
//                localWeatherDataSource.addWeather(it.weather)
//                emit(it.weather)
//            }
//            .onCompletion { cause ->
//                if (cause != null && cause !is CancellationException) {
//                    Timber.tag(TAG)
//                        .e("Error during flow completion for refreshWeather with  $cityName  $cause  in Repository")
//                }
//            }
//    }.flowOn(Dispatchers.IO)
//
//    override fun refreshWeather(lat: Double, lon: Double): Flow<WeatherDto> = flow {
//        remoteWeatherDataSource.getWeather(lat, lon)
//            .onEach {
//                localWeatherDataSource.addWeather(it.weather)
//                emit(it.weather)
//            }
//            .onCompletion { cause ->
//                if (cause != null && cause !is CancellationException) {
//                    Timber.tag(TAG)
//                        .e("Error during flow completion for getWeather with lat= $lat  $lon   $cause  in Repository")
//                }
//            }
//    }.flowOn(Dispatchers.IO)


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

}
