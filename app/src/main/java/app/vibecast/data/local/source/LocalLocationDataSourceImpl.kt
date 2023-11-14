package app.vibecast.data.local.source

import app.vibecast.data.data_repository.data_source.local.LocalLocationDataSource
import app.vibecast.data.local.db.location.LocationDao
import app.vibecast.data.local.db.location.LocationEntity
import app.vibecast.data.local.db.location.LocationWithWeatherData
import app.vibecast.domain.entity.LocationDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalLocationDataSourceImpl @Inject constructor(
    private val locationDao: LocationDao
) : LocalLocationDataSource {

    override suspend fun addLocationWithWeather(location: LocationWithWeatherData)  {
        locationDao.addLocationWithWeather(location.location, location.weather)
    }


    override fun getLocationWithWeather(): Flow<List<LocationWithWeatherData>> = locationDao.getLocationWithWeather()


    override fun getAllLocations(): Flow<List<LocationDto>> = locationDao.getLocations().map { locations ->
        locations.map {
            LocationDto(it.cityname, it.locationIndex)
        }
    }


    override fun getLocation(cityName: String): Flow<LocationDto> = locationDao.getLocation(cityName).map {
        LocationDto(it.cityname, it.locationIndex)
    }

    override suspend fun addLocation(location: LocationDto) = locationDao.addLocation(
        LocationEntity(location.cityName, location.locationIndex)
    )

    override suspend fun deleteLocation(location: LocationDto) = locationDao.deleteLocation(
        LocationEntity(location.cityName, location.locationIndex)
    )


}