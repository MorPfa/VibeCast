package app.vibecast.domain.repository.weather

import app.vibecast.domain.model.LocationDto
import app.vibecast.domain.model.LocationWithWeatherDataDto
import app.vibecast.domain.model.WeatherDto
import app.vibecast.presentation.screens.main_screen.weather.LocationModel
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