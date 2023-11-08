package app.vibecast.data.data_repository.repository

import app.vibecast.data.data_repository.data_source.local.LocalWeatherDataSource
import app.vibecast.data.data_repository.data_source.remote.RemoteWeatherDataSource
import app.vibecast.data.remote.network.weather.CoordinateApiModel
import app.vibecast.domain.entity.Weather
import app.vibecast.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val remoteWeatherDataSource: RemoteWeatherDataSource,
    private val localWeatherDataSource: LocalWeatherDataSource
) : WeatherRepository{


    override fun getCoordinates(cityName: String): Flow<CoordinateApiModel> = remoteWeatherDataSource.getCoordinates(cityName)

    override fun getWeather(cityName : String): Flow<Weather> = remoteWeatherDataSource.getWeather(cityName)


    override fun refreshWeather(cityName: String): Flow<Weather> = remoteWeatherDataSource.getWeather(cityName)
        .onEach {
            localWeatherDataSource.addWeather(it)
        }




    }
