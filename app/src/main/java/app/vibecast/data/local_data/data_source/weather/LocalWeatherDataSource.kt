package app.vibecast.data.local_data.data_source.weather

import app.vibecast.domain.model.LocationWithWeatherDataDto
import app.vibecast.domain.model.WeatherDto
import app.vibecast.domain.util.Resource


interface LocalWeatherDataSource {

    suspend fun getWeather(cityName : String) : Resource<WeatherDto>

    suspend fun getLocationWithWeather(cityName: String): Resource<LocationWithWeatherDataDto>

    suspend fun addLocationWithWeather(location: LocationWithWeatherDataDto)

    suspend fun addWeather( weather: WeatherDto)
}