package app.vibecast.data.local.db.location

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {

    @Query("SELECT * FROM locations WHERE cityName = :cityName")
    fun getLocation(cityName : String) : Flow<LocationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addLocation(locationEntity: LocationEntity)

    @Delete
    fun deleteLocation(locationEntity: LocationEntity)
}