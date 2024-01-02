package app.vibecast.data.local.db.location

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import app.vibecast.data.local.db.weather.WeatherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {


    @Transaction
    @Query("SELECT * FROM locations")
    fun getLocationsWithWeather() : Flow<List<LocationWithWeatherData>>

    @Transaction
    @Query("SELECT * FROM locations WHERE cityName = :cityName")
    fun getLocationWithWeather(cityName: String) : Flow<LocationWithWeatherData>


    @Transaction
    @Upsert
    suspend fun addLocationWithWeather(locationEntity: LocationEntity, weatherEntity: WeatherEntity)



    @Query("SELECT * FROM locations WHERE cityName = :cityName")
    fun getLocation(cityName : String) : Flow<LocationEntity>

    @Query("SELECT * FROM locations")
    fun getLocations() : Flow<List<LocationEntity>>

    @Upsert
    suspend fun addLocation(locationEntity: LocationEntity)

    @Delete
    suspend fun deleteLocation(locationEntity: LocationEntity)
}