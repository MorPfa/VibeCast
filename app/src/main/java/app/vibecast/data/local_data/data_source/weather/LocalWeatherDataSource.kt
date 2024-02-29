package app.vibecast.data.local_data.data_source.weather

import app.vibecast.domain.model.LocationWithWeatherDataDto
import app.vibecast.domain.model.WeatherDto
import kotlinx.coroutines.flow.Flow


interface LocalWeatherDataSource {

    fun getWeather(cityName : String) : Flow<WeatherDto>

    fun getLocationWithWeather(cityName: String): Flow<LocationWithWeatherDataDto>

    suspend fun addLocationWithWeather(location: LocationWithWeatherDataDto)

    suspend fun addWeather( weather: WeatherDto)
}