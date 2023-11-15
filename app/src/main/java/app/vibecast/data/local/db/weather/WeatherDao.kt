package app.vibecast.data.local.db.weather

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow


@Dao
interface WeatherDao {

    @Query("SELECT * FROM weather WHERE cityName = :cityName")
    fun getWeather(cityName: String) : Flow<WeatherEntity>

    @Upsert
    suspend fun addWeather(weatherEntity: WeatherEntity)

    @Delete
    suspend fun deleteWeather(weatherEntity: WeatherEntity)
}