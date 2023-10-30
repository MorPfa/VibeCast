package app.vibecast.data.data_repository.data_source.remote

import app.vibecast.data.remote.network.weather.CoordinateApiModel
import app.vibecast.domain.entity.Weather
import kotlinx.coroutines.flow.Flow


interface RemoteWeatherDataSource {

    fun getCity() : Flow<CoordinateApiModel>

    fun getWeather() : Flow<Weather>
}