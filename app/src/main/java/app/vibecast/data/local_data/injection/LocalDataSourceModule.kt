package app.vibecast.data.local_data.injection

import app.vibecast.data.local_data.data_source.weather.LocalLocationDataSource
import app.vibecast.data.local_data.data_source.image.LocalImageDataSource
import app.vibecast.data.local_data.data_source.user.LocalUserDataSource
import app.vibecast.data.local_data.data_source.weather.LocalWeatherDataSource
import app.vibecast.data.local_data.data_source.weather.LocalLocationDataSourceImpl
import app.vibecast.data.local_data.data_source.image.LocalImageDataSourceImpl
import app.vibecast.data.local_data.data_source.music.LocalMusicDataSource
import app.vibecast.data.local_data.data_source.music.LocalMusicDataSourceImpl
import app.vibecast.data.local_data.data_source.user.LocalUserDataSourceImpl
import app.vibecast.data.local_data.data_source.weather.LocalWeatherDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalDataSourceModule {

    @Binds
    abstract fun bindWeatherDataSource(weatherDataSourceImpl: LocalWeatherDataSourceImpl) : LocalWeatherDataSource

    @Binds
    abstract fun bindUserDataSource(userDataSourceImpl: LocalUserDataSourceImpl) : LocalUserDataSource

    @Binds
    abstract fun bindLocationDataSource(locationDataSourceImpl: LocalLocationDataSourceImpl) : LocalLocationDataSource

    @Binds
    abstract fun bindImageDataSource(pictureDataSourceImpl: LocalImageDataSourceImpl) : LocalImageDataSource

    @Binds
    abstract fun bindMusicDataSource(musicDataSourceImpl: LocalMusicDataSourceImpl) : LocalMusicDataSource
}