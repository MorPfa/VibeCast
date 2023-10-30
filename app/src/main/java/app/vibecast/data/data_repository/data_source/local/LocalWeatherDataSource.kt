package app.vibecast.data.data_repository.data_source.local

import app.vibecast.domain.entity.Weather
import kotlinx.coroutines.flow.Flow


interface LocalWeatherDataSource {

    fun getWeather(id : Int) : Flow<Weather>

    suspend fun addWeather(id : Int)
}