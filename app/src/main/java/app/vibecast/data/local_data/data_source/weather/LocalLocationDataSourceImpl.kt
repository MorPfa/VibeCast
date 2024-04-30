package app.vibecast.data.local_data.data_source.weather


import app.vibecast.data.local_data.db.location.dao.LocationDao
import app.vibecast.data.local_data.db.location.model.LocationEntity
import app.vibecast.data.local_data.db.weather.model.WeatherEntity
import app.vibecast.domain.model.LocationDto
import app.vibecast.domain.model.LocationWithWeatherDataDto
import app.vibecast.domain.model.WeatherDto
import app.vibecast.domain.repository.weather.UnitPreferenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of [LocalLocationDataSource]
 *
 * Methods:
 * - [addLocationWithWeather] Adds location data and associated weather data to database to database.
 * - [getLocationWithWeather] Queries database for all saved locations and their associated weather data
 */
class LocalLocationDataSourceImpl @Inject constructor(
    private val locationDao: LocationDao,
    private val dataStoreRepository: UnitPreferenceRepository
) : LocalLocationDataSource {



    override suspend fun addLocationWithWeather(location: LocationWithWeatherDataDto)  {
        val preferredUnit = dataStoreRepository.getPreference()
        locationDao.addLocationWithWeather(
            LocationEntity(location.location.city, location.location.country),
            WeatherEntity(
                location.location.city,
                location.location.country,
                location.weather,
                System.currentTimeMillis(),
                preferredUnit
            )
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
        LocationEntity(location.city, location.country)
    )

    override suspend fun deleteLocation(location: LocationDto) = locationDao.deleteLocation(
        LocationEntity(location.city, location.country)
    )

    override suspend fun deleteAllLocations() = locationDao.deleteAllLocations()

    private fun WeatherEntity.toWeatherDto(): WeatherDto {
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
}