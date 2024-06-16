package app.vibecast.domain.repository.weather

import app.vibecast.data.local_data.data_source.weather.LocalLocationDataSource
import app.vibecast.data.remote_data.data_source.weather.RemoteWeatherDataSource
import app.vibecast.domain.model.LocationDto
import app.vibecast.domain.model.LocationWithWeatherDataDto
import app.vibecast.domain.model.WeatherDto
import app.vibecast.domain.util.Resource
import app.vibecast.presentation.screens.main_screen.weather.LocationModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Implementation of [LocationRepository]
 *
 * Methods:
 * - [refreshLocationWeather]  Updates weather data for specified location
 */
class LocationRepositoryImpl @Inject constructor(
    private val localLocationDataSource: LocalLocationDataSource,
    private val remoteWeatherDataSource: RemoteWeatherDataSource,
) : LocationRepository {
    private val backgroundScope = CoroutineScope(Dispatchers.IO)

//    override fun refreshLocationWeather(): Flow<List<LocationWithWeatherDataDto>> {
//        backgroundScope.launch {
//            localLocationDataSource.getLocationWithWeather().map { locationWithWeatherDataList ->
//                locationWithWeatherDataList.map { locationWithWeatherData ->
//                    val cityName = locationWithWeatherData.location.city
//                    val newWeatherData = remoteWeatherDataSource.getWeather(cityName).firstOrNull()
//                    if (newWeatherData != null) {
//                        locationWithWeatherData.weather = newWeatherData.weather
//                    }
//
//                    localLocationDataSource.addLocationWithWeather(locationWithWeatherData)
//
//                }
//
//            }.single()
//        }
//        return localLocationDataSource.getLocationWithWeather()
//    }

    override fun addLocationWeather(location: LocationWithWeatherDataDto) {
        CoroutineScope(Dispatchers.Main).launch {
            localLocationDataSource.addLocationWithWeather(location)
        }
    }

    override fun getLocationWeather(index: Int): Flow<WeatherDto> {
        return localLocationDataSource.getLocationWithWeather().map { locationWithWeatherDataList ->
            if (index in locationWithWeatherDataList.indices) {
                val selectedLocationWithWeather = locationWithWeatherDataList[index]
                selectedLocationWithWeather.weather
            } else {
                WeatherDto(
                    cityName = "",
                    country = "",
                    latitude = 0.0,
                    longitude = 0.0,
                    dataTimestamp = System.currentTimeMillis(),
                    timezone = "",
                    unit = null,
                    currentWeather = null,
                    hourlyWeather = null
                )
            }
        }
    }


    override suspend fun getLocations(): Resource<List<LocationModel>> {
        return when (val locations = localLocationDataSource.getLocations()) {
            is Resource.Success -> {
                 Resource.Success(locations.data!!.toLocationModels())
            }
            is Resource.Error -> {
                Resource.Error(locations.message!!)
            }
        }
    }


    private fun List<LocationDto>.toLocationModels(): List<LocationModel> {
        return map { locationDto ->
            LocationModel(
                cityName = locationDto.city,
                country = locationDto.country
            )
        }
    }

    override fun getLocation(cityName: String): Flow<LocationDto> =
        localLocationDataSource.getLocation(cityName)

    override fun addLocation(location: LocationDto) {
        CoroutineScope(Dispatchers.IO).launch {
            localLocationDataSource.addLocation(location)
        }
    }

    override fun deleteLocation(location: LocationDto) {
        CoroutineScope(Dispatchers.IO).launch {
            localLocationDataSource.deleteLocation(location)
        }
    }

    override fun deleteAllLocations() {
        CoroutineScope(Dispatchers.IO).launch {
            localLocationDataSource.deleteAllLocations()
        }
    }
}