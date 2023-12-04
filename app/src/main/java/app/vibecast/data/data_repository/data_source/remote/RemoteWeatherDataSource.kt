package app.vibecast.data.data_repository.data_source.remote

import app.vibecast.data.remote.network.weather.CityApiModel
import app.vibecast.data.remote.network.weather.CoordinateApiModel
import app.vibecast.domain.entity.WeatherDto
import kotlinx.coroutines.flow.Flow


interface RemoteWeatherDataSource {

    fun getCoordinates(name : String) : Flow<CoordinateApiModel>

    fun getWeather(name : String) : Flow<WeatherDto>

    fun getCity(lat: Double, lon: Double) : Flow<CityApiModel>

    fun getWeather(lat : Double, lon : Double) : Flow<WeatherDto>
}