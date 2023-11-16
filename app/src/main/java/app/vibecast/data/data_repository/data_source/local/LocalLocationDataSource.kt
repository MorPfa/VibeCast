package app.vibecast.data.data_repository.data_source.local

import app.vibecast.data.local.db.location.LocationWithWeatherData
import app.vibecast.domain.entity.LocationDto
import app.vibecast.domain.entity.LocationWithWeatherDataDto
import kotlinx.coroutines.flow.Flow

interface LocalLocationDataSource {


    fun getLocationWithWeather() : Flow<List<LocationWithWeatherDataDto>>

    suspend fun addLocationWithWeather(location: LocationWithWeatherDataDto)

    fun getAllLocations() : Flow<List<LocationDto>>

    fun getLocation(cityName : String) : Flow<LocationDto>

    suspend fun addLocation(location: LocationDto)

    suspend fun deleteLocation(location: LocationDto)
}