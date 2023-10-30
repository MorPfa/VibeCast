package app.vibecast.data.local.db.weather

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather")
data class WeatherEntity (
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id : Long
    //TODO add all relevant values
)