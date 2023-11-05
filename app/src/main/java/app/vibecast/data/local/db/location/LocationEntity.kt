package app.vibecast.data.local.db.location

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class LocationEntity (
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "cityName") val cityname : String,
    @ColumnInfo(name = "lat") val lat : Double,
    @ColumnInfo(name = "lon") val lon : Double
)
