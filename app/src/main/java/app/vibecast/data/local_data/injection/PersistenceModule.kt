package app.vibecast.data.local_data.injection

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room

import app.vibecast.domain.repository.music.MusicPreferenceRepositoryImpl
import app.vibecast.domain.repository.weather.WeatherUnitRepositoryImpl
import app.vibecast.data.local_data.db.AppDatabase
import app.vibecast.data.local_data.db.location.dao.LocationDao
import app.vibecast.data.local_data.db.image.dao.ImageDao
import app.vibecast.data.local_data.db.music.dao.SongDao
import app.vibecast.data.local_data.db.user.UserDao
import app.vibecast.data.local_data.db.weather.dao.WeatherDao
import app.vibecast.domain.repository.music.MusicPreferenceRepository
import app.vibecast.domain.repository.weather.UnitPreferenceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class PersistenceModule {

    val DATASTORE_NAME = "preferences"
    @Singleton
    @Provides
    fun providesUnitPreferenceRepo(
        dataStore: DataStore<Preferences>
    ) : UnitPreferenceRepository = WeatherUnitRepositoryImpl(dataStore)

    @Singleton
    @Provides
    fun providesMusicPreferenceRepo(
        dataStore: DataStore<Preferences>
    ) : MusicPreferenceRepository = MusicPreferenceRepositoryImpl(dataStore)


    @Singleton
    @Provides
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = {context.preferencesDataStoreFile(DATASTORE_NAME)}
        )
    }

    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context) : AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "vibecast-db"
        )
            .fallbackToDestructiveMigration()
            .build()


    @Provides
    fun provideWeatherDao(appDatabase: AppDatabase) : WeatherDao = appDatabase.weatherDao()



    @Provides
    fun provideUserDao(appDatabase: AppDatabase) : UserDao = appDatabase.userDao()

    @Provides
    fun provideLocationDao(appDatabase: AppDatabase) : LocationDao = appDatabase.locationDao()

    @Provides
    fun provideImageDao(appDatabase: AppDatabase) : ImageDao = appDatabase.imageDao()

    @Provides
    fun provideSongDao(appDatabase: AppDatabase) : SongDao = appDatabase.songDao()
}