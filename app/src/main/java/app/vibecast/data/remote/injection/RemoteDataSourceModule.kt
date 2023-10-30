package app.vibecast.data.remote.injection

import app.vibecast.data.data_repository.data_source.remote.RemoteWeatherDataSource
import app.vibecast.data.remote.source.RemoteWeatherDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteDataSourceModule {

    @Binds
    abstract fun bindWeatherDataSource(weatherDataSourceImpl: RemoteWeatherDataSourceImpl) : RemoteWeatherDataSource
}