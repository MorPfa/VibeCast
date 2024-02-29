package app.vibecast.data.remote_data.injection

import app.vibecast.data.remote_data.data_source.image.RemoteImageDataSource
import app.vibecast.data.remote_data.data_source.music.RemoteMusicDataSource
import app.vibecast.data.remote_data.data_source.weather.RemoteWeatherDataSource
import app.vibecast.data.remote_data.data_source.image.RemoteImageDataSourceImpl
import app.vibecast.data.remote_data.data_source.music.RemoteMusicDataSourceImpl
import app.vibecast.data.remote_data.data_source.weather.RemoteWeatherDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteDataSourceModule {

    @Binds
    abstract fun bindWeatherDataSource(weatherDataSourceImpl: RemoteWeatherDataSourceImpl) : RemoteWeatherDataSource

    @Binds
    abstract fun bindImageDataSource(imageDataSourceImpl: RemoteImageDataSourceImpl) : RemoteImageDataSource

    @Binds
    abstract fun bindMusicDataSource(musicDataSourceImpl: RemoteMusicDataSourceImpl) : RemoteMusicDataSource
}