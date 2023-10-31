package app.vibecast.data.local.db.weather

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface WeatherDao {

    @Query("SELECT * FROM weather WHERE cityName = :cityName")
    fun getWeather(cityName: String) : Flow<WeatherEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addWeather(weatherEntity: WeatherEntity)

    @Delete
    fun deleteWeather(weatherEntity: WeatherEntity)
}