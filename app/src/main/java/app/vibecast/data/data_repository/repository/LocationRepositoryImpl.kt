package app.vibecast.data.data_repository.repository

import app.vibecast.data.data_repository.data_source.local.LocalLocationDataSource
import app.vibecast.data.data_repository.data_source.remote.RemoteWeatherDataSource
import app.vibecast.domain.entity.LocationDto
import app.vibecast.domain.entity.LocationWithWeatherDataDto
import app.vibecast.domain.entity.WeatherDto
import app.vibecast.domain.repository.LocationRepository
import app.vibecast.presentation.weather.LocationModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val localLocationDataSource: LocalLocationDataSource,
    private val remoteWeatherDataSource: RemoteWeatherDataSource
) : LocationRepository {
    private val backgroundScope = CoroutineScope(Dispatchers.IO)
    override fun refreshLocationWeather() : Flow<List<LocationWithWeatherDataDto>> {
        backgroundScope.launch {
            localLocationDataSource.getLocationWithWeather().map { locationWithWeatherDataList ->
                    locationWithWeatherDataList.map { locationWithWeatherData ->
                        val cityName = locationWithWeatherData.location.cityName
                        val newWeatherData = remoteWeatherDataSource.getWeather(cityName).firstOrNull()
                        if (newWeatherData != null) {
                            locationWithWeatherData.weather = newWeatherData.weather
                        }

                        localLocationDataSource.addLocationWithWeather(locationWithWeatherData )

                    }

                }.single()
        }
        //TODO figure out how to test this method / if return value is needed
        return localLocationDataSource.getLocationWithWeather()
    }

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
                //TODO Handle index out of bounds or other error cases here
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


    override fun getLocations(): Flow<List<LocationModel>> = flow {
        localLocationDataSource.getLocations().collect{
            emit(it.toLocationModels())
        }
    }


    private fun List<LocationDto>.toLocationModels(): List<LocationModel> {
        return map { locationDto ->
            LocationModel(
                cityName = locationDto.cityName,
                country = locationDto.country
            )
        }
    }

    override fun getLocation(cityName: String): Flow<LocationDto> = localLocationDataSource.getLocation(cityName)

    override fun addLocation(location: LocationDto) =
        CoroutineScope(Dispatchers.Main).launch {
        localLocationDataSource.addLocation(location)
    }

    override fun deleteLocation(location: LocationDto) =
        CoroutineScope(Dispatchers.IO).launch {
            localLocationDataSource.deleteLocation(location)
        }

}