package app.vibecast.data.data_repository.repository

import app.vibecast.data.data_repository.data_source.local.LocalLocationDataSource
import app.vibecast.data.data_repository.data_source.remote.RemoteWeatherDataSource
import app.vibecast.data.local.db.location.LocationWithWeatherData
import app.vibecast.data.local.db.weather.WeatherEntity
import app.vibecast.domain.entity.LocationDto
import app.vibecast.domain.entity.WeatherDto
import app.vibecast.domain.repository.LocationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val localLocationDataSource: LocalLocationDataSource,
    private val remoteWeatherDataSource: RemoteWeatherDataSource
) : LocationRepository {
    private val backgroundScope = CoroutineScope(Dispatchers.IO)
    override fun refreshLocationWeather() : Flow<List<LocationWithWeatherData>> {

        backgroundScope.launch {
            localLocationDataSource.getLocationWithWeather().map { locationWithWeatherDataList ->
                    locationWithWeatherDataList.map { locationWithWeatherData ->
                        val cityName = locationWithWeatherData.location.cityname

                        // Asynchronously fetch the latest weather data for the city from the remote data source
                        val newWeatherData = remoteWeatherDataSource.getWeather(cityName).firstOrNull()

                        // Update the weather data in the combined entity
                        if (newWeatherData != null) {
                            locationWithWeatherData.weather = newWeatherData.toWeatherEntity(cityName)
                        }

                        // Save the updated location with weather entity to the database
                        localLocationDataSource.addLocationWithWeather(locationWithWeatherData )

                    }

                }.single()
        }
        //TODO figure out how to test this method / if return value is needed
        return localLocationDataSource.getLocationWithWeather()
    }

    override fun addLocationWeather(location: LocationWithWeatherData) {
        CoroutineScope(Dispatchers.Main).launch {
        localLocationDataSource.addLocationWithWeather(location)
        }
    }
    override fun getLocationWeather(index: Int): Flow<WeatherDto> {
        return localLocationDataSource.getLocationWithWeather().map { locationWithWeatherDataList ->
            if (index in locationWithWeatherDataList.indices) {
                val selectedLocationWithWeather = locationWithWeatherDataList[index]
                selectedLocationWithWeather.weather.weatherData
            } else {
                //TODO Handle index out of bounds or other error cases here
                WeatherDto(cityName = "", latitude = 0.0, longitude = 0.0, currentWeather = null, hourlyWeather = null)
            }
        }
    }


    override fun getLocations(): Flow<List<LocationDto>> = localLocationDataSource.getAllLocations()


    override fun getLocation(cityName: String): Flow<LocationDto> = localLocationDataSource.getLocation(cityName)

    override fun addLocation(location: LocationDto) =  CoroutineScope(Dispatchers.Main).launch {
        localLocationDataSource.addLocation(location)
    }

    override fun deleteLocation(location: LocationDto) =
        CoroutineScope(Dispatchers.Main).launch {
            localLocationDataSource.deleteLocation(location)
        }


    private fun WeatherDto.toWeatherEntity(cityName: String): WeatherEntity {
        return WeatherEntity(
            cityName = cityName,
            weatherData = this
        )
    }
}