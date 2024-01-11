package app.vibecast.data.remote.network.image

import app.vibecast.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import retrofit2.http.Url


interface ImageService {

    companion object {
        const val CLIENT_ID = BuildConfig.UNSPLASH_KEY
    }

    @Headers("Authorization: Client-ID $CLIENT_ID")
    @GET
    suspend fun getImageForDownload(
        @Url query: String,
    ) : DownloadUrl

    @Headers("Accept-Version: v1", "Authorization: Client-ID $CLIENT_ID")
    @GET("photos/random")
    suspend fun getImages(
        @Query("query") query: String,
        @Query("orientation") orientation : String,
        @Query("count") count : Int,
        @Query("content_filter") contentFilter : String
    ) : List<ImageApiModel>

}