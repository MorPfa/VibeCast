package app.vibecast.domain.repository

import app.vibecast.data.remote.network.weather.CoordinateApiModel
import app.vibecast.domain.entity.Weather
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {

    fun getCoordinates(cityName: String) : Flow<CoordinateApiModel>

    fun getWeather(cityName : String) : Flow<Weather>

    fun refreshWeather(cityName : String) : Flow<Weather>

    fun refreshWeather(lat : Double, lon : Double) : Flow<Weather>
}