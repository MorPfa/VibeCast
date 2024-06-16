package app.vibecast.domain.repository.weather

import app.vibecast.domain.model.LocationWithWeatherDataDto
import app.vibecast.domain.model.WeatherDto
import app.vibecast.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface WeatherRepository {

    suspend fun getWeather(cityName: String): Resource<LocationWithWeatherDataDto>

    suspend fun getSearchedWeather(cityName: String): Resource<LocationWithWeatherDataDto>

    suspend fun getWeather(lat: Double, lon: Double): Resource<LocationWithWeatherDataDto>
//    suspend fun refreshWeather(cityName: String): Resource<WeatherDto>
//
//    suspend fun refreshWeather(lat: Double, lon: Double): Resource<WeatherDto>


}