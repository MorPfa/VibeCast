package app.vibecast.data.remote.network.image

import app.vibecast.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query


interface ImageService {

    companion object {
        const val CLIENT_ID = BuildConfig.UNSPLASH_KEY
    }

    @Headers("Accept-Version: v1", "Authorization: Client-ID $CLIENT_ID")
    @GET("/search/photos")
    suspend fun getImages(
        @Query("query") query: String,
        @Query("orientation") orientation : String
    ) : ImageApiModel

}