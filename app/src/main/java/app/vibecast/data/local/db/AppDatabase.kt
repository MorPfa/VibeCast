package app.vibecast.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import app.vibecast.data.local.db.weather.WeatherEntity
import app.vibecast.data.local.db.weather.WeatherDao


@Database(entities = [WeatherEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun weatherDao() : WeatherDao
}