package app.vibecast.data.data_repository.data_source.local

import app.vibecast.data.local.db.location.LocationWithWeatherData
import app.vibecast.domain.entity.Location
import kotlinx.coroutines.flow.Flow

interface LocalLocationDataSource {


    fun getLocationWithWeather() : Flow<List<LocationWithWeatherData>>

    suspend fun addLocationWithWeather(location: LocationWithWeatherData)

    fun getAllLocations() : Flow<List<Location>>

    fun getLocation(cityName : String) : Flow<Location>

    suspend fun addLocation(location: Location)

    suspend fun deleteLocation(location: Location)
}