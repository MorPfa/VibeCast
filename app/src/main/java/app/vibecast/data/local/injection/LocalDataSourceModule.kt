package app.vibecast.data.local.injection

import app.vibecast.data.data_repository.data_source.local.LocalUserDataSource
import app.vibecast.data.data_repository.data_source.local.LocalWeatherDataSource
import app.vibecast.data.data_repository.data_source.remote.RemoteWeatherDataSource
import app.vibecast.data.local.source.LocalUserDataSourceImpl
import app.vibecast.data.local.source.LocalWeatherDataSourceImpl
import app.vibecast.data.remote.source.RemoteWeatherDataSourceImpl
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
}