package app.vibecast.domain.repository

import app.vibecast.domain.entity.LocationDto
import app.vibecast.domain.entity.LocationWithWeatherDataDto
import app.vibecast.domain.entity.WeatherDto
import app.vibecast.presentation.weather.LocationModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

interface  LocationRepository {


    fun refreshLocationWeather() : Flow<List<LocationWithWeatherDataDto>>
    fun getLocationWeather(index : Int) : Flow<WeatherDto>


    fun addLocationWeather(location : LocationWithWeatherDataDto)

    fun getLocations() : Flow<List<LocationModel>>

    fun getLocation(cityName : String) : Flow<LocationDto>

    fun addLocation(location: LocationDto): Job

    fun deleteLocation(location: LocationDto): Job
}