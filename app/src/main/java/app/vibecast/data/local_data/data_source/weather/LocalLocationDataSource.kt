package app.vibecast.data.local_data.data_source.weather

import app.vibecast.domain.model.LocationDto
import app.vibecast.domain.model.LocationWithWeatherDataDto
import kotlinx.coroutines.flow.Flow

interface LocalLocationDataSource {


    fun getLocationWithWeather() : Flow<List<LocationWithWeatherDataDto>>

    suspend fun addLocationWithWeather(location: LocationWithWeatherDataDto)

    fun getLocations() : Flow<List<LocationDto>>

    fun getLocation(cityName : String) : Flow<LocationDto>

    suspend fun addLocation(location: LocationDto)

    suspend fun deleteLocation(location: LocationDto)
    suspend fun deleteAllLocations()
}