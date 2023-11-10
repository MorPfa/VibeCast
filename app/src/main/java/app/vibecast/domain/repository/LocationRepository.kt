package app.vibecast.domain.repository

import app.vibecast.data.local.db.location.LocationWithWeatherData
import app.vibecast.domain.entity.Location
import app.vibecast.domain.entity.Weather
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

interface  LocationRepository {


    fun refreshLocationWeather()
    fun getLocationWeather(index : Int) : Flow<Weather>


    fun addLocationWeather(location : LocationWithWeatherData)

    fun getLocations() : Flow<List<Location>>

    fun getLocation(cityName : String) : Flow<Location>

    fun addLocation(location: Location): Job

    fun deleteLocation(location: Location): Job
}