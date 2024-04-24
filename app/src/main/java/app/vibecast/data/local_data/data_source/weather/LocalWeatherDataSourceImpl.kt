package app.vibecast.data.local_data.data_source.weather

import app.vibecast.data.local_data.db.location.dao.LocationDao
import app.vibecast.data.local_data.db.location.model.LocationEntity
import app.vibecast.data.local_data.db.weather.dao.WeatherDao
import app.vibecast.data.local_data.db.weather.model.WeatherEntity
import app.vibecast.domain.model.LocationDto
import app.vibecast.domain.model.LocationWithWeatherDataDto
import app.vibecast.domain.model.WeatherDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject


/**
 * Implementation of [LocalWeatherDataSource]
 *
 * Methods:
 * - [getWeather] Queries database for last saved weather data for specified city and returns weather Data Transfer Objects.
 * - [getLocationWithWeather] Queries database for saved location and and associated weather data.
 * - [addLocationWithWeather] Adds location with associated weather data to database.
 * - [toWeather] Converts DB Entity for weather data into Data Transfer Object.
 * - [toWeatherEntity] Converts weather Data Transfer Object to DB Entity
 */

class LocalWeatherDataSourceImpl @Inject constructor(
    private val weatherDao: WeatherDao,
    private val locationDao: LocationDao
) : LocalWeatherDataSource {

    override fun getWeather(cityName: String): Flow<WeatherDto> = weatherDao.getWeather(cityName).map {
            weatherEntity -> weatherEntity.toWeather()
    }.flowOn(Dispatchers.IO)


    override fun getLocationWithWeather(cityName: String): Flow<LocationWithWeatherDataDto> =
        locationDao.getLocationWithWeather(cityName)
            .map { locationWithWeatherEntity ->
                locationWithWeatherEntity.let {

                    val locationDto = LocationDto(
                        city = it.location.cityName,
                        country = it.location.country
                    )

                    val weatherDto = it.weather.weatherData

                    LocationWithWeatherDataDto(location = locationDto, weather = weatherDto)
                }
            }


    override suspend fun addLocationWithWeather(location: LocationWithWeatherDataDto) {
        locationDao.addLocationWithWeather(
            LocationEntity(location.location.city, location.location.country),
            location.weather.toWeatherEntity(location.location.city)
        )
    }

    override suspend fun addWeather(weather : WeatherDto) {
       weatherDao.addWeather(weather.toWeatherEntity(weather.cityName))
    }

    private fun WeatherEntity.toWeather(): WeatherDto {
        return WeatherDto(
            cityName = cityName,
            country = countryName,
            latitude = weatherData.latitude,
            longitude = weatherData.longitude,
            dataTimestamp = dataTimestamp,
            timezone = weatherData.timezone,
            unit = unit,
            currentWeather = weatherData.currentWeather,
            hourlyWeather = weatherData.hourlyWeather
        )
    }

    private fun WeatherDto.toWeatherEntity(cityName: String): WeatherEntity {
        return WeatherEntity(
            cityName = cityName,
            countryName = this.country,
            weatherData = this,
            dataTimestamp = this.dataTimestamp,
            unit = this.unit
        )
    }
}