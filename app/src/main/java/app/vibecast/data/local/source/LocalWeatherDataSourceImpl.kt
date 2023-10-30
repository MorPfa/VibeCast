package app.vibecast.data.local.source

import app.vibecast.data.data_repository.data_source.local.LocalWeatherDataSource
import app.vibecast.data.data_repository.data_source.remote.RemoteWeatherDataSource
import app.vibecast.data.remote.network.weather.WeatherService
import app.vibecast.domain.entity.Weather
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

//TODO add userDao to constructor
class LocalWeatherDataSourceImpl @Inject constructor( ) :
    LocalWeatherDataSource {

    override fun getWeather(id : Int): Flow<Weather> {
        TODO("Not yet implemented")
    }

    override suspend fun addWeather(id: Int) {
        TODO("Not yet implemented")
    }


}