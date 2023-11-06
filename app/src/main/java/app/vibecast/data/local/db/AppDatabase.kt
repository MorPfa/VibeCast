package app.vibecast.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.vibecast.data.local.db.location.LocationDao
import app.vibecast.data.local.db.location.LocationEntity
import app.vibecast.data.local.db.picture.PictureDao
import app.vibecast.data.local.db.picture.PictureEntity
import app.vibecast.data.local.db.user.UserDao
import app.vibecast.data.local.db.user.UserEntity
import app.vibecast.data.local.db.weather.WeatherDao
import app.vibecast.data.local.db.weather.WeatherEntity
import app.vibecast.data.local.db.weather.WeatherTypeConverter

@TypeConverters(WeatherTypeConverter::class)
@Database(entities = [WeatherEntity::class, UserEntity::class, LocationEntity::class, PictureEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun weatherDao() : WeatherDao

    abstract fun userDao() : UserDao
    abstract fun locationDao() : LocationDao

    abstract fun pictureDao() : PictureDao
}