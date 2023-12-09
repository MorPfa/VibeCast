package app.vibecast.data.local.db.location

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class LocationEntity (
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "cityName") val cityName : String,
    @ColumnInfo(name = "country") val country : String,

    )
