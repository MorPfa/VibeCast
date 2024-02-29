package app.vibecast.data.remote_data.data_source.weather

import app.vibecast.data.remote_data.network.weather.model.CityApiModel
import app.vibecast.data.remote_data.network.weather.model.CoordinateApiModel
import app.vibecast.domain.model.LocationWithWeatherDataDto
import kotlinx.coroutines.flow.Flow


interface RemoteWeatherDataSource {

    fun getCoordinates(name : String) : Flow<CoordinateApiModel>

    fun getCity(lat: Double, lon: Double) : Flow<CityApiModel>

    fun getWeather(cityName : String) : Flow<LocationWithWeatherDataDto>

    fun getWeather(lat : Double, lon : Double) : Flow<LocationWithWeatherDataDto>
}