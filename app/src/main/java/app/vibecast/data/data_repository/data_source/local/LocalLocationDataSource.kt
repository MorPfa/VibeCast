package app.vibecast.data.data_repository.data_source.local

import app.vibecast.data.local.db.location.LocationWithWeatherData
import app.vibecast.domain.entity.Location
import app.vibecast.domain.entity.Weather
import kotlinx.coroutines.flow.Flow

interface LocalLocationDataSource {


    fun getLocationWithWeather(location: Location) : Flow<List<LocationWithWeatherData>>

    fun addLocationWithWeather(location: Location, weather: Weather)

    fun getAllLocations() : Flow<List<Location>>

    fun getLocation(cityName : String) : Flow<Location>

    fun addLocation(location: Location)

    fun deleteLocation(location: Location)
}