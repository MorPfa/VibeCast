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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val remoteWeatherDataSource: RemoteWeatherDataSource,
    private val localWeatherDataSource: LocalWeatherDataSource,
) : WeatherRepository{


    override fun getCoordinates(cityName: String): Flow<CoordinateApiModel> =
        remoteWeatherDataSource.getCoordinates(cityName)

    override fun getWeather(cityName: String): Flow<LocationWithWeatherDataDto> = flow {
        remoteWeatherDataSource.getWeather(cityName).collect{
            emit(it)

        }
    }.flowOn(Dispatchers.IO)


    override fun getWeather(lat: Double, lon: Double): Flow<LocationWithWeatherDataDto> = flow {
        remoteWeatherDataSource.getCity(lat, lon).collect { data ->
            val cityName = data.cityName

            if (cityName.isNotBlank()) {
                val localWeatherFlow = localWeatherDataSource.getLocationWithWeather(cityName)
                val localWeatherData = localWeatherFlow.firstOrNull()
                if (localWeatherData != null) {
                    // Location found in the local database, emit the data
//                    Log.d(TAG, localWeatherData.weather.cityName)
                    emit(localWeatherData)
                } else {
                    // Location not found in the local database, fetch from the remote source
                    remoteWeatherDataSource.getWeather(lat, lon)
                        .collect {
                            it.location.cityName = data.cityName
                            it.location.country = data.countryName
                            Log.d(TAG, data.cityName)
                            Log.d(TAG, data.countryName)
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

    }.flowOn(Dispatchers.IO)


    override fun refreshWeather(lat: Double, lon: Double): Flow<WeatherDto> = flow {
        remoteWeatherDataSource.getWeather(lat, lon)
            .onEach {
                localWeatherDataSource.addWeather(it.weather)
                emit(it.weather)
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
