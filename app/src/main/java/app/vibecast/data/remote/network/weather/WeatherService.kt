package app.vibecast.data.remote.network.weather

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("geo/1.0/direct")
    suspend fun getCiyCoordinates(
        @Query("q") cityName: String,
        @Query("limit") limit: Int,
        @Query("appid") apiKey: String): List<CoordinateApiModel>


    @GET("data/3.0/onecall")
    suspend fun getWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("exclude") exclude: String,
        @Query("appid") apiKey: String
    ) : WeatherApiModel

}