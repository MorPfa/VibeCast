package app.vibecast.domain.repository.music

import app.vibecast.data.remote_data.network.music.model.PlaylistApiModel
import app.vibecast.data.remote_data.network.music.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {

    fun getPlaylist(category : String, accessCode : String) : Flow<PlaylistApiModel>

    fun getCurrentSong(song : String, artist : String, accessCode : String) : Flow<Song>

    suspend fun deleteSong(song: Song)

    suspend fun saveSong(song : Song)

    fun getSavedSong(song : Song) : Flow<Song>

    fun getAllSavedSongs() : Flow<Song>
}