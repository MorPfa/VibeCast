package app.vibecast.domain.injection


import app.vibecast.domain.repository.weather.LocationRepositoryImpl
import app.vibecast.domain.repository.image.ImageRepositoryImpl
import app.vibecast.domain.repository.music.MusicRepositoryImpl
import app.vibecast.domain.repository.user.UserRepositoryImpl
import app.vibecast.domain.repository.weather.WeatherRepositoryImpl

import app.vibecast.domain.repository.weather.LocationRepository
import app.vibecast.domain.repository.image.ImageRepository
import app.vibecast.domain.repository.music.MusicRepository
import app.vibecast.domain.repository.user.UserRepository
import app.vibecast.domain.repository.weather.WeatherRepository
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