package app.vibecast.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.vibecast.data.local.db.user.UserDao
import app.vibecast.data.local.db.user.UserEntity
import app.vibecast.data.local.db.weather.WeatherDao
import app.vibecast.data.local.db.weather.WeatherEntity
import app.vibecast.data.local.db.weather.WeatherTypeConverter

@TypeConverters(WeatherTypeConverter::class)
@Database(entities = [WeatherEntity::class, UserEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun weatherDao() : WeatherDao

    abstract fun userDao() : UserDao
}