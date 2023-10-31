package app.vibecast.data.data_repository.data_source.local

import app.vibecast.domain.entity.Weather
import kotlinx.coroutines.flow.Flow


interface LocalWeatherDataSource {

    fun getWeather(cityName : String) : Flow<Weather>

    suspend fun addWeather(cityName : String, weather: Weather)
}