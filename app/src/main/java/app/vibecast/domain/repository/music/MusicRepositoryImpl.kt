package app.vibecast.domain.repository.music

import app.vibecast.data.local_data.data_source.music.LocalMusicDataSource
import app.vibecast.data.remote_data.data_source.music.RemoteMusicDataSource
import app.vibecast.data.remote_data.network.music.model.PlaylistApiModel
import app.vibecast.data.remote_data.network.music.model.Song
import kotlinx.coroutines.flow.Flow

import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class MusicRepositoryImpl @Inject constructor(
    private val remoteMusicDataSource: RemoteMusicDataSource,
    private val localMusicDataSource: LocalMusicDataSource,
) : MusicRepository {


    override suspend fun deleteSong(song: Song) {
        try {
            localMusicDataSource.deleteSong(song)
        } catch (e : Exception){

        }
    }

    override fun getAllSavedSongs(): Flow<Song> = flow {
        try {
            localMusicDataSource.getAllSavedSongs().collect {
            }
        } catch (e: Exception){

        }
    }

    override suspend fun saveSong(song: Song) {
        try {
            localMusicDataSource.saveSong(song)
        } catch (e : Exception){

        }
    }

    override fun getSavedSong(song : Song): Flow<Song> = flow {
        try {
            localMusicDataSource.getSavedSong(song).collect {
            }
        } catch (e: Exception){

        }
    }

    override fun getPlaylist(category: String, accessCode: String): Flow<PlaylistApiModel> = flow {
        try {
            remoteMusicDataSource.getPlaylist(category, accessCode).collect {
                emit(it)
            }
        } catch (e: Exception) {
            Timber.tag("Spotify").d(e.localizedMessage ?: "null")
        }

    }


    override fun getCurrentSong(
        song: String,
        artist: String,
        accessCode: String,
    ): Flow<Song> = flow {
        try {
            remoteMusicDataSource.getCurrentSong(song, artist, accessCode).collect { result ->
                emit(result.songs[0])
            }
        } catch (e: Exception) {
            Timber.tag("Spotify").d(e.localizedMessage ?: "null")
        }
    }
}