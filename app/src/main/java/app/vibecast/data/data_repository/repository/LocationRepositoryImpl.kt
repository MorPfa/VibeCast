package app.vibecast.data.data_repository.repository

import app.vibecast.data.data_repository.data_source.local.LocalLocationDataSource
import app.vibecast.data.data_repository.data_source.remote.RemoteWeatherDataSource
import app.vibecast.data.local.db.location.LocationWithWeatherData
import app.vibecast.data.local.db.weather.WeatherEntity
import app.vibecast.domain.entity.Location
import app.vibecast.domain.entity.Weather
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
    override fun refreshLocationWeather() {
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
    }

    override fun addLocationWeather(location: LocationWithWeatherData) {
        CoroutineScope(Dispatchers.Main).launch {
        localLocationDataSource.addLocationWithWeather(location)
        }
    }
    override fun getLocationWeather(index: Int): Flow<Weather> {
        return localLocationDataSource.getLocationWithWeather().map { locationWithWeatherDataList ->
            if (index in locationWithWeatherDataList.indices) {
                val selectedLocationWithWeather = locationWithWeatherDataList[index]
                selectedLocationWithWeather.weather.weatherData
            } else {
                // Handle index out of bounds or other error cases here
                // You can return a default value, throw an exception, or handle it as needed.
                // For now, returning an empty Weather object as a placeholder:
                Weather(cityName = "", latitude = null, longitude = null, currentWeather = null, hourlyWeather = null)
            }
        }
    }


    override fun getLocations(): Flow<List<Location>> = localLocationDataSource.getAllLocations()


    override fun getLocation(cityName: String): Flow<Location> = localLocationDataSource.getLocation(cityName)

    override fun addLocation(location: Location) =  CoroutineScope(Dispatchers.Main).launch {
        localLocationDataSource.addLocation(location)
    }

    override fun deleteLocation(location: Location) =
        CoroutineScope(Dispatchers.Main).launch {
            localLocationDataSource.deleteLocation(location)
        }


    private fun Weather.toWeatherEntity(cityName: String): WeatherEntity {
        return WeatherEntity(
            cityName = cityName,
            weatherData = this
        )
    }
}