package app.vibecast.data.data_repository.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import app.vibecast.data.data_repository.data_source.local.LocalWeatherDataSource
import app.vibecast.data.data_repository.data_source.remote.RemoteWeatherDataSource
import app.vibecast.data.remote.network.weather.CoordinateApiModel
import app.vibecast.domain.entity.WeatherDto
import app.vibecast.domain.repository.WeatherRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val remoteWeatherDataSource: RemoteWeatherDataSource,
    private val localWeatherDataSource: LocalWeatherDataSource,
    @ApplicationContext private val context: Context
) : WeatherRepository{


    override fun getCoordinates(cityName: String): Flow<CoordinateApiModel> =
        remoteWeatherDataSource.getCoordinates(cityName)

    override fun getWeather(cityName: String): Flow<WeatherDto> {
        return if (isInternetAvailable(context)) {
            remoteWeatherDataSource.getWeather(cityName)
        } else {
            localWeatherDataSource.getWeather(cityName)
        }.flowOn(Dispatchers.IO)
    }



    override fun refreshWeather(cityName: String): Flow<WeatherDto> =
        remoteWeatherDataSource.getWeather(cityName)
            .flowOn(Dispatchers.IO)
            .onEach {
                localWeatherDataSource.addWeather(it)
        }


    override fun refreshWeather(lat: Double, lon: Double): Flow<WeatherDto> =
        remoteWeatherDataSource.getWeather(lat, lon)
            .flowOn(Dispatchers.IO)
            .onEach {
                localWeatherDataSource.addWeather(it)
    }


    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }



}
