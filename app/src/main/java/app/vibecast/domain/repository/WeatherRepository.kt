package app.vibecast.domain.repository

import app.vibecast.data.remote.network.weather.CoordinateApiModel
import app.vibecast.domain.entity.WeatherDto
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {

    fun getCoordinates(cityName: String) : Flow<CoordinateApiModel>

    fun getWeather(cityName : String) : Flow<WeatherDto>

    fun refreshWeather(cityName : String) : Flow<WeatherDto>

    fun refreshWeather(lat : Double, lon : Double) : Flow<WeatherDto>
}