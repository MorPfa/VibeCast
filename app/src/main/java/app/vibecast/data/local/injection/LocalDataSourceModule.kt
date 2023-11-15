package app.vibecast.data.local.injection

import app.vibecast.data.data_repository.data_source.local.LocalLocationDataSource
import app.vibecast.data.data_repository.data_source.local.LocalImageDataSource
import app.vibecast.data.data_repository.data_source.local.LocalUserDataSource
import app.vibecast.data.data_repository.data_source.local.LocalWeatherDataSource
import app.vibecast.data.local.source.LocalLocationDataSourceImpl
import app.vibecast.data.local.source.LocalImageDataSourceImpl
import app.vibecast.data.local.source.LocalUserDataSourceImpl
import app.vibecast.data.local.source.LocalWeatherDataSourceImpl
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
}