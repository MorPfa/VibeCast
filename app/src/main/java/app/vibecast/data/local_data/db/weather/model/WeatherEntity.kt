package app.vibecast.data.local_data.db.weather.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import app.vibecast.domain.repository.weather.Unit
import app.vibecast.domain.model.WeatherDto

@Entity(tableName = "weather")
data class WeatherEntity(
    @PrimaryKey(autoGenerate = false) @ColumnInfo(name = "cityName") val cityName: String,
    @ColumnInfo(name = "countryName") val countryName: String,
    @ColumnInfo(name = "weatherData") val weatherData: WeatherDto,
    @ColumnInfo(name = "dataTimestamp") val dataTimestamp: Long,
    @ColumnInfo(name = "unit") val unit: Unit?

)