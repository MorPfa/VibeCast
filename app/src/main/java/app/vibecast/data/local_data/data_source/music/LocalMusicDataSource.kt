package app.vibecast.data.local_data.data_source.music

import app.vibecast.data.remote_data.network.music.model.Song
import kotlinx.coroutines.flow.Flow

interface LocalMusicDataSource {

    suspend fun saveSong(song: Song)

    suspend fun deleteSong(song: Song)

    fun getSavedSong(song: Song) : Flow<Song>
    fun getAllSavedSongs() : Flow<List<Song>>
}