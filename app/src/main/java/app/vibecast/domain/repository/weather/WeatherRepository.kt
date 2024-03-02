package app.vibecast.domain.repository.weather

import app.vibecast.domain.model.LocationWithWeatherDataDto
import app.vibecast.domain.model.WeatherDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface WeatherRepository {


    fun getWeather(cityName : String) : Flow<LocationWithWeatherDataDto>

    fun getSearchedWeather(cityName : String) : Flow<LocationWithWeatherDataDto>

    fun getWeather(lat: Double, lon: Double): Flow<LocationWithWeatherDataDto>
    fun refreshWeather(cityName : String) : Flow<WeatherDto>

    fun refreshWeather(lat : Double, lon : Double) : Flow<WeatherDto>

    val currentWeather : MutableStateFlow<String?>
}