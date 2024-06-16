package app.vibecast.data.local_data.db.weather.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import app.vibecast.data.local_data.db.weather.model.WeatherEntity


@Dao
interface WeatherDao {

    @Query("SELECT * FROM weather WHERE cityName = :cityName")
    suspend fun getWeather(cityName: String) : WeatherEntity?

    @Upsert
    suspend fun addWeather(weatherEntity: WeatherEntity)

    @Delete
    suspend fun deleteWeather(weatherEntity: WeatherEntity)
}