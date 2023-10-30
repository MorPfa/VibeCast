package app.vibecast.data.data_repository.repository

import app.vibecast.data.data_repository.data_source.local.LocalWeatherDataSource
import app.vibecast.data.data_repository.data_source.remote.RemoteWeatherDataSource
import app.vibecast.domain.entity.Weather
import app.vibecast.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val remoteWeatherDataSource: RemoteWeatherDataSource,
    private val localWeatherDataSource: LocalWeatherDataSource
    //TODO add localWeatherDataSource to get cached weather data
) : WeatherRepository{
    override fun getWeather(id : Int ): Flow<Weather> = remoteWeatherDataSource.getWeather()
        .onEach {
            localWeatherDataSource.addWeather(id)
        }


    }
