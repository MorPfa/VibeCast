package app.vibecast.data.remote_data.injection

import android.app.Application
import app.vibecast.data.remote_data.network.image.api.ImageService
import app.vibecast.data.remote_data.network.music.api.MusicService
import app.vibecast.data.remote_data.network.weather.api.WeatherService
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named

/**
 * Hilt Module to provide network related utilities
 * Methods:
- All methods provide dependencies for Hilt but some of them have commented out versions that include Http loggers
 */

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {


    
    @Provides
    fun provideMoshi(): Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Provides
    fun provideGson(): Gson = GsonBuilder().create()


//    @Named("music")
//    @Provides
//    fun provideSpotifyRetrofit(): Retrofit = Retrofit.Builder()
//        .baseUrl("https://api.spotify.com/")
//        .addConverterFactory(GsonConverterFactory.create())
//        .build()

    @Named("unsplash")
    @Provides
    fun provideUnsplashRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.unsplash.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Named("music")
    @Provides
    fun provideSpotifyRetrofit(): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
        return Retrofit.Builder()
            .baseUrl("https://api.spotify.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()


    }


    @Named("weather")
    @Provides
    fun provideWeatherRetrofit(moshi: Moshi): Retrofit {
//        val loggingInterceptor = HttpLoggingInterceptor().apply {
//            level = HttpLoggingInterceptor.Level.BODY
//        }
//
//        val client = OkHttpClient.Builder()
//            .addInterceptor(loggingInterceptor)
//            .build()

        return Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
//            .client(client)
            .build()
    }


//    @Named("unsplash")
//    @Provides
//    fun provideUnsplashRetrofit(): Retrofit {
//        val loggingInterceptor = HttpLoggingInterceptor().apply {
//            level = HttpLoggingInterceptor.Level.BODY
//        }
//
//        val client = OkHttpClient.Builder()
//            .addInterceptor(loggingInterceptor)
//            .build()
//        return Retrofit.Builder()
//            .baseUrl("https://api.unsplash.com/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(client)
//            .build()
//
//    }


    @Provides
    fun provideWeatherService(@Named("weather") retrofit: Retrofit): WeatherService =
        retrofit.create(WeatherService::class.java)

    @Provides
    fun provideMusicService(@Named("music") retrofit: Retrofit): MusicService =
        retrofit.create(MusicService::class.java)


    @Provides
    fun provideImageService(@Named("unsplash") retrofit: Retrofit): ImageService =
        retrofit.create(ImageService::class.java)

    @Provides
    fun provideGlideInstance(application: Application): RequestManager = Glide.with(application)
}