package app.vibecast.domain.repository.music

import app.vibecast.data.remote_data.network.music.model.PlaylistApiModel
import app.vibecast.domain.model.SongDto
import app.vibecast.domain.model.TracksDto
import kotlinx.coroutines.flow.Flow

interface MusicRepository {

    fun getPlaylist(category : String, accessCode : String) : Flow<PlaylistApiModel>

    fun getCurrentSong(song : String, artist : String, accessCode : String) : Flow<TracksDto>

    suspend fun deleteSong(song: SongDto)

    suspend fun saveSong(song : SongDto)

    fun getSavedSong(song : SongDto) : Flow<SongDto>

    fun getAllSavedSongs() : Flow<List<SongDto>>
}