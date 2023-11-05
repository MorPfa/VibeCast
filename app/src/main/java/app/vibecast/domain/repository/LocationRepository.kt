package app.vibecast.domain.repository

import app.vibecast.domain.entity.Location
import kotlinx.coroutines.flow.Flow

interface  LocationRepository {

    fun getLocation(cityName : String) : Flow<Location>

    fun addLocation(location: Location)

    fun deleteLocation(location: Location)
}