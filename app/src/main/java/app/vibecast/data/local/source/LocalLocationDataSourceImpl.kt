package app.vibecast.data.local.source

import app.vibecast.data.data_repository.data_source.local.LocalLocationDataSource
import app.vibecast.data.local.db.location.LocationDao
import app.vibecast.data.local.db.location.LocationEntity
import app.vibecast.data.local.db.location.LocationWithWeatherData
import app.vibecast.data.local.db.weather.WeatherEntity
import app.vibecast.domain.entity.Location
import app.vibecast.domain.entity.Weather
import kotlinx.coroutines.flow.Flow

import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalLocationDataSourceImpl @Inject constructor(private val locationDao: LocationDao) : LocalLocationDataSource {

    override fun addLocationWithWeather(location: Location, weather: Weather) {
        locationDao.addLocationWithWeather(
            LocationWithWeatherData(
                LocationEntity(location.cityName, location.locationIndex),
                weather.toWeatherEntity(location.cityName)
            )
        )
    }

    override fun getLocationWithWeather(location: Location): Flow<List<LocationWithWeatherData>> = locationDao.getLocationWithWeather()


    override fun getAllLocations(): Flow<List<Location>> = locationDao.getLocations().map { locations ->
        locations.map {
            Location(it.cityname, it.locationIndex)
        }
    }


    override fun getLocation(cityName: String): Flow<Location> = locationDao.getLocation(cityName).map {
        Location(it.cityname, it.locationIndex)
    }

    override fun addLocation(location: Location) = locationDao.addLocation(
        LocationEntity(location.cityName, location.locationIndex)
    )

    override fun deleteLocation(location: Location) = locationDao.deleteLocation(
        LocationEntity(location.cityName, location.locationIndex)
    )

    private fun Weather.toWeatherEntity(cityName: String): WeatherEntity {
        return WeatherEntity(
            cityName = cityName,
            weatherData = this
        )
    }
}