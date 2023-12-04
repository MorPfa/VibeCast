package app.vibecast.data.data_repository.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import app.vibecast.data.data_repository.data_source.local.LocalWeatherDataSource
import app.vibecast.data.data_repository.data_source.remote.RemoteWeatherDataSource
import app.vibecast.data.remote.network.weather.CoordinateApiModel
import app.vibecast.domain.entity.CurrentWeather
import app.vibecast.domain.entity.HourlyWeather
import app.vibecast.domain.entity.LocationDto
import app.vibecast.domain.entity.LocationWithWeatherDataDto
import app.vibecast.domain.entity.WeatherDto
import app.vibecast.domain.repository.WeatherRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val remoteWeatherDataSource: RemoteWeatherDataSource,
    private val localWeatherDataSource: LocalWeatherDataSource,
    @ApplicationContext private val context: Context
) : WeatherRepository{


    override fun getCoordinates(cityName: String): Flow<CoordinateApiModel> =
        remoteWeatherDataSource.getCoordinates(cityName)

    override fun getWeather(cityName: String): Flow<LocationWithWeatherDataDto> {
        return if (isInternetAvailable(context)) {
            remoteWeatherDataSource.getWeather(cityName)
                .map { weatherDto ->
                    convertWeatherToLocationWithWeather(weatherDto)
                }
        } else {
            localWeatherDataSource.getLocationWithWeather(cityName)
        }.flowOn(Dispatchers.IO)
    }


    override fun getWeather(lat: Double, lon: Double): Flow<LocationWithWeatherDataDto> = flow {
        remoteWeatherDataSource.getCity(lat, lon).collect { data ->
            val cityName = data.cityName
            if (cityName.isNotBlank()) {
                val localWeatherFlow = localWeatherDataSource.getLocationWithWeather(cityName)
                val localWeatherData = localWeatherFlow.firstOrNull()

                if (localWeatherData != null) {
                    // Location found in the local database, emit the data
                    emit(localWeatherData)
                } else {
                    // Location not found in the local database, fetch from the remote source
                    remoteWeatherDataSource.getWeather(cityName)
                        .map { weatherDto ->
                            convertWeatherToLocationWithWeather(weatherDto)
                        }.collect {
                            emit(it)
                        }
                }
            }
        }
    }.flowOn(Dispatchers.IO)





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



    private fun convertWeatherToLocationWithWeather(weatherDto: WeatherDto): LocationWithWeatherDataDto {
        val convertedCurrentWeather = convertCurrentWeather(weatherDto.currentWeather)
        val convertedHourlyWeather = weatherDto.hourlyWeather?.map { convertHourlyWeather(it) }
        return LocationWithWeatherDataDto(
            location = LocationDto(cityName = weatherDto.cityName, locationIndex = 0), // Replace with the actual location data
            weather = WeatherDto(
                cityName = weatherDto.cityName,
                latitude = weatherDto.latitude,
                longitude = weatherDto.longitude,
                currentWeather = convertedCurrentWeather,
                hourlyWeather = convertedHourlyWeather
            )
        )
    }

    private fun convertCurrentWeather(dto: CurrentWeather?): CurrentWeather? {
        return dto?.let {
            CurrentWeather(
                timestamp = dto.timestamp,
                temperature = dto.temperature,
                feelsLike = dto.feelsLike,
                humidity = dto.humidity,
                uvi = dto.uvi,
                cloudCover = dto.cloudCover,
                visibility = dto.visibility,
                windSpeed = dto.windSpeed,
                weatherConditions = dto.weatherConditions
            )
        }
    }

    private fun convertHourlyWeather(dto: HourlyWeather): HourlyWeather {
        return HourlyWeather(
            timestamp = dto.timestamp,
            temperature = dto.temperature,
            feelsLike = dto.feelsLike,
            humidity = dto.humidity,
            uvi = dto.uvi,
            windSpeed = dto.windSpeed,
            weatherConditions = dto.weatherConditions,
            chanceOfRain = dto.chanceOfRain
        )
    }
}
