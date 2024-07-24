package app.vibecast.domain.repository.music

import app.vibecast.data.local_data.db.music.model.UserPlaylistEntity
import app.vibecast.data.remote_data.network.music.model.AddToPlaylistPayload
import app.vibecast.data.remote_data.network.music.model.RemoveFromPlaylistPayload
import app.vibecast.domain.model.PlaylistDto
import app.vibecast.domain.model.SelectedPlaylistDto
import app.vibecast.domain.model.SongDto
import app.vibecast.domain.model.TracksDto
import app.vibecast.domain.model.UserDto
import app.vibecast.domain.model.UserPlaylistDto
import app.vibecast.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface MusicRepository {

    suspend fun getSelectedPlaylist(id : String, accessCode: String) : Resource<SelectedPlaylistDto>
    suspend fun getPlaylist(category: String, accessCode: String): Resource<PlaylistDto>

    suspend fun getCurrentSong(
        song: String,
        artist: String,
        accessCode: String,
    ): Resource<TracksDto>

    suspend fun deleteSong(song: SongDto)
    suspend fun deleteAllSongs()

    suspend fun saveSong(song: SongDto)

    fun getSavedSong(song: SongDto): Flow<SongDto>

    fun getAllSavedSongs(): Flow<List<SongDto>>


    suspend fun addSongToPlaylist(
        userId: String,
        playlistName: String,
        accessCode: String,
        data: AddToPlaylistPayload,
    ): Resource<Void>

    suspend fun deleteSongFromPlaylist(
        playlistId: String,
        accessCode: String,
        data: RemoveFromPlaylistPayload,
    ): Resource<Void>

    suspend fun deletePlaylist(playlistId : String,  accessCode: String): Resource<Void>


    suspend fun getCurrentUser(accessCode : String) : Resource<UserDto>

    suspend fun doesPlaylistExist(cityName: String): Resource<UserPlaylistEntity>

     suspend fun getUserPlaylists(accessCode: String): Resource<UserPlaylistDto>

}