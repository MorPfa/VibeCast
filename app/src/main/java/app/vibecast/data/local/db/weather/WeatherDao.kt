package app.vibecast.data.local.db.weather

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface WeatherDao {

    @Query("SELECT * FROM weather")
    fun getWeather() : Flow<WeatherEntity>
}