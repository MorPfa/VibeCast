package app.vibecast.domain.repository

import app.vibecast.data.remote.network.weather.CoordinateApiModel
import app.vibecast.domain.entity.LocationWithWeatherDataDto
import app.vibecast.domain.entity.WeatherDto
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {

    fun getCoordinates(cityName: String) : Flow<CoordinateApiModel>

    fun getWeather(cityName : String) : Flow<LocationWithWeatherDataDto>

    fun getSearchedWeather(cityName : String) : Flow<LocationWithWeatherDataDto>

    fun getWeather(lat: Double, lon: Double): Flow<LocationWithWeatherDataDto>
    fun refreshWeather(cityName : String) : Flow<WeatherDto>

    fun refreshWeather(lat : Double, lon : Double) : Flow<WeatherDto>
}