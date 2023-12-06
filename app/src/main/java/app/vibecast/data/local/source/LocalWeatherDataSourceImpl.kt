package app.vibecast.data.local.source

import app.vibecast.data.data_repository.data_source.local.LocalWeatherDataSource
import app.vibecast.data.local.db.location.LocationDao
import app.vibecast.data.local.db.weather.WeatherDao
import app.vibecast.data.local.db.weather.WeatherEntity
import app.vibecast.domain.entity.LocationDto
import app.vibecast.domain.entity.LocationWithWeatherDataDto
import app.vibecast.domain.entity.WeatherDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class LocalWeatherDataSourceImpl @Inject constructor(
    private val weatherDao: WeatherDao,
    private val locationDao: LocationDao
) : LocalWeatherDataSource {
    override fun getWeather(cityName: String): Flow<WeatherDto> = weatherDao.getWeather(cityName).map {
            weatherEntity -> weatherEntity.toWeather()
    }.flowOn(Dispatchers.IO)

    override fun getLocationWithWeather(cityName: String): Flow<LocationWithWeatherDataDto?> =
        locationDao.getLocationWithWeather(cityName)
            .map { locationWithWeatherEntity ->
                locationWithWeatherEntity?.let {
                    val locationDto = LocationDto(
                        cityName = it.location.cityname,
                        locationIndex = it.location.locationIndex
                    )

                    val weatherDto = it.weather.weatherData // Assuming weatherData is a WeatherDto

                    LocationWithWeatherDataDto(location = locationDto, weather = weatherDto)
                }
            }





    override suspend fun addWeather(weather : WeatherDto) {
       weatherDao.addWeather(weather.toWeatherEntity(weather.cityName))
    }


    private fun WeatherEntity.toWeather(): WeatherDto {
        return WeatherDto(
            cityName = cityName,
            latitude = weatherData.latitude,
            longitude = weatherData.longitude,
            currentWeather = weatherData.currentWeather,
            hourlyWeather = weatherData.hourlyWeather
        )
    }
    private fun WeatherDto.toWeatherEntity(cityName: String): WeatherEntity {
        return WeatherEntity(
            cityName = cityName,
            weatherData = this
        )
    }
}