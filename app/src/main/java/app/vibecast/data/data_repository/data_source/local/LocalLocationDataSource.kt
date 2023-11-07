package app.vibecast.data.data_repository.data_source.local

import app.vibecast.domain.entity.Location
import kotlinx.coroutines.flow.Flow

interface LocalLocationDataSource {

    fun getAllLocations() : Flow<List<Location>>

    fun getLocation(cityName : String) : Flow<Location>

    fun addLocation(location: Location)

    fun deleteLocation(location: Location)
}