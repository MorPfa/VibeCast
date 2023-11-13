package app.vibecast.data.remote.injection

import app.vibecast.data.remote.network.picture.PictureService
import app.vibecast.data.remote.network.weather.WeatherService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named


@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    fun provideMoshi(): Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    @Provides
    fun provideGson(): Gson = GsonBuilder().create()

    @Named("weather")
    @Provides
    fun provideWeatherRetrofit(moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()


    @Provides
    fun provideWeatherService(@Named("weather") retrofit: Retrofit) : WeatherService =
        retrofit.create(WeatherService::class.java)

    //TODO add glide for image transformation
    @Named("unsplash")
    @Provides
    fun provideUnsplashRetrofit(moshi: Moshi) : Retrofit = Retrofit.Builder()
        .baseUrl("https://api.unsplash.com/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()


    @Provides
    fun providePictureService(@Named("unsplash") retrofit: Retrofit) : PictureService =
        retrofit.create(PictureService::class.java)
}