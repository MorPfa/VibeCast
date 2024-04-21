package app.vibecast.data.remote_data.network.music.api


import app.vibecast.data.remote_data.network.music.model.PlaylistApiModel
import app.vibecast.data.remote_data.network.music.model.TracksResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface MusicService {


    @GET("browse/categories/{category_id}/playlists")
    suspend fun getPlaylist(
        @Path("category_id") categoryId: String,
        @Header("Authorization") accessCode: String,
        @Query("limit") limit: Int =10,
        @Query("offset") offset: Int =0
    ) : PlaylistApiModel


    @GET("search")
    suspend fun getCurrentSong(
        @Header("Authorization") accessCode: String,
        @Query("q") query : String,
        @Query("type") type : String = "track",
        @Query("limit") limit: Int =5,
        @Query("offset") offset: Int =0
    ) : TracksResponse
}