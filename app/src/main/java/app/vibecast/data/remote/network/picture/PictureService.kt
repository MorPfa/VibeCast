package app.vibecast.data.remote.network.picture

import retrofit2.http.GET
import retrofit2.http.Query

interface PictureService {

    @GET("/search/photos")
    suspend fun getCiyCoordinates(
        @Query("query") cityName: String,
        @Query("collections") collectionId: String)

}