package app.vibecast.data.local.db.weather

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import app.vibecast.data.data_repository.repository.Unit
import app.vibecast.domain.entity.WeatherDto

@Entity(tableName = "weather")
data class WeatherEntity(
    @PrimaryKey(autoGenerate = false) @ColumnInfo(name = "cityName") val cityName: String,
    @ColumnInfo(name = "countryName") val countryName: String,
    @ColumnInfo(name = "weatherData") val weatherData: WeatherDto,
    @ColumnInfo(name = "dataTimestamp") val dataTimestamp: Long,
    @ColumnInfo(name = "unit") val unit: Unit?

)