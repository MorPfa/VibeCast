package app.vibecast.data.data_repository.data_source.local

import app.vibecast.domain.entity.WeatherDto
import kotlinx.coroutines.flow.Flow


interface LocalWeatherDataSource {

    fun getWeather(cityName : String) : Flow<WeatherDto>

    suspend fun addWeather( weather: WeatherDto)
}