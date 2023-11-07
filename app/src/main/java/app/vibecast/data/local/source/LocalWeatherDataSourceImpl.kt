package app.vibecast.data.local.source

import app.vibecast.data.data_repository.data_source.local.LocalWeatherDataSource
import app.vibecast.data.local.db.weather.WeatherDao
import app.vibecast.data.local.db.weather.WeatherEntity
import app.vibecast.domain.entity.Weather
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class LocalWeatherDataSourceImpl @Inject constructor(private val weatherDao: WeatherDao ) :
    LocalWeatherDataSource {
    override fun getWeather(cityName: String): Flow<Weather> = weatherDao.getWeather(cityName).map {
            weatherEntity -> weatherEntity.toWeather()
    }

    override suspend fun addWeather( weather : Weather) {
       weatherDao.addWeather(weather.toWeatherEntity(weather.cityName))
    }


    private fun WeatherEntity.toWeather(): Weather {
        return Weather(
            cityName = cityName,
            latitude = weatherData.latitude,
            longitude = weatherData.longitude,
            currentWeather = weatherData.currentWeather,
            hourlyWeather = weatherData.hourlyWeather
        )
    }
    private fun Weather.toWeatherEntity(cityName: String): WeatherEntity {
        return WeatherEntity(
            cityName = cityName,
            weatherData = this
        )
    }




}