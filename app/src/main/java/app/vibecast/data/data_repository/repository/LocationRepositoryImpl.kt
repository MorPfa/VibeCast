package app.vibecast.data.data_repository.repository

import app.vibecast.data.data_repository.data_source.local.LocalLocationDataSource
import app.vibecast.domain.entity.Location
import app.vibecast.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val localLocationDataSource: LocalLocationDataSource
) : LocationRepository {


    override fun getLocations(): Flow<List<Location>> = localLocationDataSource.getAllLocations()


    override fun getLocation(cityName: String): Flow<Location> = localLocationDataSource.getLocation(cityName)

    override fun addLocation(location: Location) = localLocationDataSource.addLocation(location)

    override fun deleteLocation(location: Location) = localLocationDataSource.deleteLocation(location)
}