package app.vibecast.data.remote_data.data_source.weather

import app.vibecast.data.remote_data.network.weather.model.CityApiModel
import app.vibecast.data.remote_data.network.weather.model.CoordinateApiModel
import app.vibecast.domain.model.LocationWithWeatherDataDto
import app.vibecast.domain.util.Resource
import kotlinx.coroutines.flow.Flow


interface RemoteWeatherDataSource {

    suspend fun getCoordinates(name: String): Resource<CoordinateApiModel>
    suspend fun getSearchCoordinates(name: String): Resource<CoordinateApiModel>

    suspend fun getCity(lat: Double, lon: Double): Resource<CityApiModel>

    suspend fun getWeather(cityName: String): Resource<LocationWithWeatherDataDto>
    suspend fun getSearchedWeather(cityName: String): Resource<LocationWithWeatherDataDto>

    suspend fun getWeather(lat: Double, lon: Double): Resource<LocationWithWeatherDataDto>
}