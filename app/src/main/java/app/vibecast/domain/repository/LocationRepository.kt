package app.vibecast.domain.repository

import app.vibecast.data.local.db.location.LocationWithWeatherData
import app.vibecast.domain.entity.LocationDto
import app.vibecast.domain.entity.WeatherDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

interface  LocationRepository {


    fun refreshLocationWeather() : Flow<List<LocationWithWeatherData>>
    fun getLocationWeather(index : Int) : Flow<WeatherDto>


    fun addLocationWeather(location : LocationWithWeatherData)

    fun getLocations() : Flow<List<LocationDto>>

    fun getLocation(cityName : String) : Flow<LocationDto>

    fun addLocation(location: LocationDto): Job

    fun deleteLocation(location: LocationDto): Job
}