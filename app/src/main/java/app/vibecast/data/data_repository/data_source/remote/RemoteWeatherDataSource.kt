package app.vibecast.data.data_repository.data_source.remote

import app.vibecast.data.remote.network.weather.CoordinateApiModel
import app.vibecast.domain.entity.WeatherDto
import kotlinx.coroutines.flow.Flow


interface RemoteWeatherDataSource {

    fun getCoordinates(name : String) : Flow<CoordinateApiModel>

    fun getWeather(name : String) : Flow<WeatherDto>

    fun getWeather(lat : Double, lon : Double) : Flow<WeatherDto>
}