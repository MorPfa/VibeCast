package app.vibecast.data.remote_data.data_source.music


import app.vibecast.data.remote_data.network.music.model.AddToPlaylistPayload
import app.vibecast.data.remote_data.network.music.model.CreatePlaylistPayload
import app.vibecast.data.remote_data.network.music.model.RemoveFromPlaylistPayload
import app.vibecast.domain.model.PlaylistCreatedDto
import app.vibecast.domain.model.PlaylistDto
import app.vibecast.domain.model.SelectedPlaylistDto
import app.vibecast.domain.model.TracksDto
import app.vibecast.domain.model.UserDto
import app.vibecast.domain.model.UserPlaylistDto
import app.vibecast.domain.util.Resource

interface RemoteMusicDataSource {
    suspend fun getSelectedPlaylist(id : String, accessCode: String) : Resource<SelectedPlaylistDto>
    suspend fun getPlaylist(category: String, accessCode: String): Resource<PlaylistDto>

    suspend fun getCurrentSong(song: String, artist: String, accessCode: String): Resource<TracksDto>

    suspend fun createPlaylist(userId : String, accessCode: String, data : CreatePlaylistPayload) : Resource<PlaylistCreatedDto>

    suspend fun addSongToPlaylist(userId : String, playlistId : String,  accessCode: String, data : AddToPlaylistPayload) : Resource<Void>

    suspend fun deleteSongFromPlaylist(playlistId : String,  accessCode: String, data : RemoveFromPlaylistPayload) : Resource<Void>

    suspend fun deletePlaylist(playlistId : String,  accessCode: String) : Resource<Void>

    suspend fun getUserPlaylists( accessCode: String) : Resource<UserPlaylistDto>

    suspend fun doesPlaylistExist(playlistId : String,  accessCode: String) : Boolean

    suspend fun getCurrentUser(accessCode: String) : Resource<UserDto>
}