package app.vibecast.domain.repository

import app.vibecast.domain.entity.Weather
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {

    fun getWeather(id : Int) : Flow<Weather>
}