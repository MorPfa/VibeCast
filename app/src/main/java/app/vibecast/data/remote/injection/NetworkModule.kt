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


@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    fun provideMoshi(): Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    @Provides
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    fun provideWeatherRetrofit(moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    fun provideWeatherService(retrofit: Retrofit) : WeatherService =
        retrofit.create(WeatherService::class.java)

    //TODO add glide for image transformation
    @Provides
    fun provideUnsplashRetrofit(moshi: Moshi) : Retrofit = Retrofit.Builder()
        .baseUrl("https://api.unsplash.com/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()


    @Provides
    fun providePictureService(retrofit: Retrofit) : PictureService =
        retrofit.create(PictureService::class.java)
}