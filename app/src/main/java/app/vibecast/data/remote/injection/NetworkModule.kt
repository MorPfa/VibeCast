package app.vibecast.data.remote.injection

import android.app.Application
import app.vibecast.data.remote.network.image.ImageService
import app.vibecast.data.remote.network.weather.WeatherService
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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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

//    @Named("weather")
//    @Provides
//    fun provideWeatherRetrofit(moshi: Moshi): Retrofit {
//        val loggingInterceptor = HttpLoggingInterceptor().apply {
//            level = HttpLoggingInterceptor.Level.BODY
//        }
//
//        val client = OkHttpClient.Builder()
//            .addInterceptor(loggingInterceptor)
//            .build()
//
//        return Retrofit.Builder()
//            .baseUrl("https://api.openweathermap.org/")
//            .addConverterFactory(MoshiConverterFactory.create(moshi))
//            .client(client)
//            .build()
//    }


    @Provides
    fun provideWeatherService(@Named("weather") retrofit: Retrofit) : WeatherService =
        retrofit.create(WeatherService::class.java)


//    @Named("unsplash")
//    @Provides
//    fun provideUnsplashRetrofit() : Retrofit {
//        val loggingInterceptor = HttpLoggingInterceptor().apply {
//            level = HttpLoggingInterceptor.Level.BODY
//        }
//
//        val client = OkHttpClient.Builder()
//            .addInterceptor(loggingInterceptor)
//            .build()
//       return  Retrofit.Builder()
//            .baseUrl("https://api.unsplash.com/")
//            .addConverterFactory(GsonConverterFactory.create())
//           .client(client)
//            .build()
//
//    }

    @Named("unsplash")
    @Provides
    fun provideUnsplashRetrofit() : Retrofit =  Retrofit.Builder()
            .baseUrl("https://api.unsplash.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()



    @Provides
    fun provideImageService(@Named("unsplash") retrofit: Retrofit) : ImageService =
        retrofit.create(ImageService::class.java)

    @Provides
    fun provideGlideInstance(application: Application) : RequestManager = Glide.with(application)
}