package app.vibecast.data.local.source

import app.vibecast.data.data_repository.data_source.local.LocalLocationDataSource
import app.vibecast.data.local.db.location.LocationDao
import app.vibecast.data.local.db.location.LocationEntity
import app.vibecast.domain.entity.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalLocationDataSourceImpl @Inject constructor(private val locationDao: LocationDao) : LocalLocationDataSource {


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
}