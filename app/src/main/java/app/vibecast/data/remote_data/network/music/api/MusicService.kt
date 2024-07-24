package app.vibecast.data.remote_data.network.music.api


import app.vibecast.data.remote_data.network.music.model.PlaylistApiModel
import app.vibecast.data.remote_data.network.music.model.CreatePlaylistPayload
import app.vibecast.data.remote_data.network.music.model.TracksResponse
import app.vibecast.data.remote_data.network.music.model.AddToPlaylistPayload
import app.vibecast.data.remote_data.network.music.model.PlaylistCheck
import app.vibecast.data.remote_data.network.music.model.PlaylistCreated
import app.vibecast.data.remote_data.network.music.model.RemoveFromPlaylistPayload
import app.vibecast.data.remote_data.network.music.model.User
import app.vibecast.data.remote_data.network.music.model.UserPlaylist
import app.vibecast.data.remote_data.network.music.model.UserPlaylists
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MusicService {



    @GET("playlists/{playlist_id}")
    suspend fun getPlaylistById(
        @Path("playlist_id") playlistId: String,
        @Header("Authorization") accessCode: String,
        @Query("market") market: String?,
        @Query("fields") fields: String,
    ): Response<UserPlaylist>

    @GET("browse/categories/{category_id}/playlists")
    suspend fun getPlaylist(
        @Path("category_id") categoryId: String,
        @Header("Authorization") accessCode: String,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0,
    ): Response<PlaylistApiModel>

    @GET("me")
    suspend fun getCurrentUser(
        @Header("Authorization") accessCode: String,
    ): Response<User>


    @GET("playlists/{playlist_id}/followers/contains")
    suspend fun doesPlaylistExist(
        @Header("Authorization") accessCode: String,
        @Path("playlist_id") playlistId: String,
    ): Response<PlaylistCheck>

    @GET("me/playlists")
    suspend fun getUserPlaylists(
        @Header("Authorization") accessCode: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
    ): Response<UserPlaylists>


    @GET("search")
    suspend fun getCurrentSong(
        @Header("Authorization") accessCode: String,
        @Query("q") query: String,
        @Query("type") type: String = "track",
        @Query("limit") limit: Int = 3,
        @Query("offset") offset: Int = 0,
    ): Response<TracksResponse>


    @POST("users/{user}/playlists")
    suspend fun createPlaylist(
        @Path("user") userId: String,
        @Header("Authorization") accessCode: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body data: CreatePlaylistPayload,
    ): Response<PlaylistCreated>

    @POST("playlists/{playlist_id}/tracks")
    suspend fun addSongToPlaylist(
        @Path("playlist_id") playlistId: String,
        @Header("Authorization") accessCode: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body data: AddToPlaylistPayload,
    ): Response<Void>

    @DELETE("/playlists/{playlist_id}/tracks")
    suspend fun deleteSongFromPlaylist(
        @Path("playlist_id") playlistId: String,
        @Header("Authorization") accessCode: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body data: RemoveFromPlaylistPayload,
    ): Response<Void>

    @DELETE("/playlists/{playlist_id}/followers")
    suspend fun deletePlaylist(
        @Path("playlist_id") playlistId: String,
        @Header("Authorization") accessCode: String,
    ): Response<Void>
}