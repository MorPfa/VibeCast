package app.vibecast.data.local_data.data_source.music

import app.vibecast.data.local_data.db.music.model.UserPlaylistEntity
import app.vibecast.domain.model.SongDto
import kotlinx.coroutines.flow.Flow

interface LocalMusicDataSource {

    suspend fun saveSong(song: SongDto)

    suspend fun deleteSong(song: SongDto)
    suspend fun deleteAllSongs()

    fun getSavedSong(song: SongDto) : Flow<SongDto>
    fun getAllSavedSongs() : Flow<List<SongDto>>

    fun getAllPlaylists() : Flow<List<UserPlaylistEntity>>

    suspend fun savePlaylist(playlist: UserPlaylistEntity)

}