package app.vibecast.data.local_data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.vibecast.data.local_data.db.location.dao.LocationDao
import app.vibecast.data.local_data.db.location.model.LocationEntity
import app.vibecast.data.local_data.db.image.dao.ImageDao
import app.vibecast.data.local_data.db.image.model.ImageEntity
import app.vibecast.data.local_data.db.music.dao.SongDao
import app.vibecast.data.local_data.db.music.model.SongEntity
import app.vibecast.data.local_data.db.user.UserDao
import app.vibecast.data.local_data.db.user.UserEntity

import app.vibecast.data.local_data.db.weather.dao.WeatherDao
import app.vibecast.data.local_data.db.weather.model.WeatherEntity
import app.vibecast.data.local_data.db.weather.util.WeatherTypeConverter

@TypeConverters(WeatherTypeConverter::class)
@Database(
    entities = [
        WeatherEntity::class,
        UserEntity::class,
        LocationEntity::class,
        ImageEntity::class,
        SongEntity::class,
    ], version = 15
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao

    abstract fun userDao(): UserDao
    abstract fun locationDao(): LocationDao

    abstract fun imageDao(): ImageDao

    abstract fun songDao(): SongDao
}