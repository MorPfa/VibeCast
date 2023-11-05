package app.vibecast.data.local.injection

import android.content.Context
import androidx.room.Room
import app.vibecast.data.local.db.AppDatabase
import app.vibecast.data.local.db.location.LocationDao
import app.vibecast.data.local.db.user.UserDao
import app.vibecast.data.local.db.weather.WeatherDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
class PersistenceModule {


    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context) : AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java, "vibecast-db"
        ).build()


    @Provides
    fun provideWeatherDao(appDatabase: AppDatabase) : WeatherDao = appDatabase.weatherDao()



    @Provides
    fun provideUserDao(appDatabase: AppDatabase) : UserDao = appDatabase.userDao()

    @Provides
    fun provideLocationDao(appDatabase: AppDatabase) : LocationDao = appDatabase.locationDao()
}