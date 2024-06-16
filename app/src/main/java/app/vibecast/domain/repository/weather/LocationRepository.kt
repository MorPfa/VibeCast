package app.vibecast.domain.repository.weather

import app.vibecast.domain.model.LocationDto
import app.vibecast.domain.model.LocationWithWeatherDataDto
import app.vibecast.domain.model.WeatherDto
import app.vibecast.domain.util.Resource
import app.vibecast.presentation.screens.main_screen.weather.LocationModel
import kotlinx.coroutines.flow.Flow

interface LocationRepository {


//    fun refreshLocationWeather(): Flow<List<LocationWithWeatherDataDto>>
    fun getLocationWeather(index: Int): Flow<WeatherDto>

    fun addLocationWeather(location: LocationWithWeatherDataDto)

    suspend fun getLocations(): Resource<List<LocationModel>>

    fun getLocation(cityName: String): Flow<LocationDto>

    fun addLocation(location: LocationDto)

    fun deleteLocation(location: LocationDto)
    fun deleteAllLocations()
}