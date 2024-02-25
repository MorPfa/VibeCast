package app.vibecast.data.remote.network.music


import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface MusicService {


    @GET("browse/categories/{category_id}/playlists")
    suspend fun getPlaylist(
        @Path("category_id") categoryId: String,
        @Header("Authorization") accessCode: String,
        @Query("limit") limit: Int =1,
        @Query("offset") offset: Int =0
    ) : PlaylistApiModel
}