package app.vibecast.data.local.source


import app.vibecast.data.data_repository.data_source.local.LocalLocationDataSource
import app.vibecast.data.local.db.location.LocationDao
import app.vibecast.data.local.db.location.LocationEntity
import app.vibecast.data.local.db.weather.WeatherEntity
import app.vibecast.domain.entity.LocationDto
import app.vibecast.domain.entity.LocationWithWeatherDataDto
import app.vibecast.domain.entity.WeatherDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalLocationDataSourceImpl @Inject constructor(
    private val locationDao: LocationDao
) : LocalLocationDataSource {

    override suspend fun addLocationWithWeather(location: LocationWithWeatherDataDto)  {
        locationDao.addLocationWithWeather(
            LocationEntity(location.location.cityName, location.location.country),
            WeatherEntity(location.location.cityName, location.weather)
            )
    }


    override fun getLocationWithWeather(): Flow<List<LocationWithWeatherDataDto>> =
        locationDao.getLocationsWithWeather().map { locationWithWeatherList ->
            locationWithWeatherList.map { locationWithWeatherData ->
                LocationWithWeatherDataDto(
                    location = LocationDto(locationWithWeatherData.location.cityName, locationWithWeatherData.location.country),
                    weather = locationWithWeatherData.weather.toWeatherDto() // Assuming a conversion function
                )
            }
        }


    override fun getLocations(): Flow<List<LocationDto>> = locationDao.getLocations().map { locations ->
        locations.map {
            LocationDto(it.cityName, it.country)
        }
    }


    override fun getLocation(cityName: String): Flow<LocationDto> = locationDao.getLocation(cityName).map {
        LocationDto(it.cityName, it.country)
    }

    override suspend fun addLocation(location: LocationDto) = locationDao.addLocation(
        LocationEntity(location.cityName, location.country)
    )

    override suspend fun deleteLocation(location: LocationDto) = locationDao.deleteLocation(
        LocationEntity(location.cityName, location.country)
    )


    private fun WeatherEntity.toWeatherDto(): WeatherDto {
        return WeatherDto(
            cityName = cityName,
            latitude = weatherData.latitude,
            longitude = weatherData.longitude,
            timezone = weatherData.timezone,
            currentWeather = weatherData.currentWeather,
            hourlyWeather = weatherData.hourlyWeather
        )
    }


}