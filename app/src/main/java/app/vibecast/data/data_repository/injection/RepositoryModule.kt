package app.vibecast.data.data_repository.injection

import app.vibecast.data.data_repository.repository.UserRepositoryImpl
import app.vibecast.data.data_repository.repository.WeatherRepositoryImpl
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
}