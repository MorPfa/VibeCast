package app.vibecast.domain.repository

import app.vibecast.data.local.db.location.LocationWithWeatherData
import app.vibecast.domain.entity.Location
import kotlinx.coroutines.flow.Flow

interface  LocationRepository {


    fun refreshLocationWeather(location: Location) : Flow<List<LocationWithWeatherData>>

    fun getLocations() : Flow<List<Location>>

    fun getLocation(cityName : String) : Flow<Location>

    fun addLocation(location: Location)

    fun deleteLocation(location: Location)
}