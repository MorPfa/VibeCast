package app.vibecast.data.local.db.weather

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import app.vibecast.domain.entity.Weather

@Entity(tableName = "weather")
data class WeatherEntity (
    @PrimaryKey(autoGenerate = false) @ColumnInfo(name = "cityName") val cityName : String,
    @ColumnInfo(name = "weatherData") val weatherData : Weather
)