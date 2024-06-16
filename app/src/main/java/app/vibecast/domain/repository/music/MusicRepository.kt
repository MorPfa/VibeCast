package app.vibecast.domain.repository.music

import app.vibecast.domain.model.PlaylistDto
import app.vibecast.domain.model.SongDto
import app.vibecast.domain.model.TracksDto
import app.vibecast.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface MusicRepository {

    suspend fun getPlaylist(category: String, accessCode: String): Resource<PlaylistDto>

    suspend fun getCurrentSong(song: String, artist: String, accessCode: String): Resource<TracksDto>

    suspend fun deleteSong(song: SongDto)
    suspend fun deleteAllSongs()

    suspend fun saveSong(song: SongDto)

    fun getSavedSong(song: SongDto): Flow<SongDto>

    fun getAllSavedSongs(): Flow<List<SongDto>>
}