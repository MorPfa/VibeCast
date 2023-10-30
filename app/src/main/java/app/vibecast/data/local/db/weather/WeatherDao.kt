package app.vibecast.data.local.db.weather

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface WeatherDao {

    @Query("SELECT * FROM weather")
    fun getWeather() : Flow<WeatherEntity>

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun addWeather()

//    @Delete
//    fun deleteWeather(id : Int)
}