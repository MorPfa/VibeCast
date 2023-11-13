package app.vibecast.data.local.db.location

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import app.vibecast.data.local.db.weather.WeatherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {


    @Transaction
    @Query("SELECT * FROM locations")
    fun getLocationWithWeather() : Flow<List<LocationWithWeatherData>>


    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addLocationWithWeather(locationEntity: LocationEntity, weatherEntity: WeatherEntity)

    @Query("SELECT * FROM locations WHERE cityName = :cityName")
    fun getLocation(cityName : String) : Flow<LocationEntity>

    @Query("SELECT * FROM locations ")
    fun getLocations() : Flow<List<LocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addLocation(locationEntity: LocationEntity)

    @Delete
    suspend fun deleteLocation(locationEntity: LocationEntity)
}