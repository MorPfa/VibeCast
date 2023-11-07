package app.vibecast.domain.repository

import app.vibecast.domain.entity.Weather
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {

    fun getWeather(cityName : String) : Flow<Weather>

    fun refreshWeather(cityName : String) : Flow<Weather>
}