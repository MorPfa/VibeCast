package app.vibecast.domain.injection


import app.vibecast.domain.repository.implementation.LocationRepositoryImpl
import app.vibecast.domain.repository.implementation.ImageRepositoryImpl
import app.vibecast.domain.repository.implementation.MusicRepositoryImpl
import app.vibecast.domain.repository.implementation.UserRepositoryImpl
import app.vibecast.domain.repository.implementation.WeatherRepositoryImpl

import app.vibecast.domain.repository.LocationRepository
import app.vibecast.domain.repository.ImageRepository
import app.vibecast.domain.repository.MusicRepository
import app.vibecast.domain.repository.UserRepository
import app.vibecast.domain.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindWeatherRepository(weatherRepositoryImpl: WeatherRepositoryImpl) : WeatherRepository

    @Binds
    abstract fun bindUserRepository(userRepositoryImpl: UserRepositoryImpl) : UserRepository

    @Binds
    abstract fun bindLocationRepository(locationRepositoryImpl: LocationRepositoryImpl) : LocationRepository

    @Binds
    abstract  fun bindImageRepository(imageRepositoryImpl: ImageRepositoryImpl) : ImageRepository

    @Binds
    abstract  fun bindMusicRepository(musicRepositoryImpl: MusicRepositoryImpl) : MusicRepository



}