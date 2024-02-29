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


class LocalWeatherDataSourceImpl @Inject constructor(
    private val weatherDao: WeatherDao,
    private val locationDao: LocationDao
) : LocalWeatherDataSource {

    /**
     *  Queries database for last saved weather data in specific city
     */
    override fun getWeather(cityName: String): Flow<WeatherDto> = weatherDao.getWeather(cityName).map {
            weatherEntity -> weatherEntity.toWeather()
    }.flowOn(Dispatchers.IO)


    /**
     *  Queries database for last saved weather data in specific city along with detailed location data
     */
    override fun getLocationWithWeather(cityName: String): Flow<LocationWithWeatherDataDto> =
        locationDao.getLocationWithWeather(cityName)
            .map { locationWithWeatherEntity ->
                locationWithWeatherEntity.let {

                    val locationDto = LocationDto(
                        cityName = it.location.cityName,
                        country = it.location.country
                    )

                    val weatherDto = it.weather.weatherData

                    LocationWithWeatherDataDto(location = locationDto, weather = weatherDto)
                }
            }


    /**
     *  Adds weather data and location data to database
     */
    override suspend fun addLocationWithWeather(location: LocationWithWeatherDataDto) {
        locationDao.addLocationWithWeather(
            LocationEntity(location.location.cityName, location.location.country),
            location.weather.toWeatherEntity(location.location.cityName)
        )
    }
    /**
     *  Adds weather data to database
     */
    override suspend fun addWeather(weather : WeatherDto) {
       weatherDao.addWeather(weather.toWeatherEntity(weather.cityName))
    }

    /**
     *  Converts DB Entity for weather data into Data Transfer Object
     */
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

    /**
     *  Converts Data Transfer Object for weather data into DB Entity
     */
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