package app.vibecast.domain.repository.music

import app.vibecast.data.local_data.data_source.music.LocalMusicDataSource
import app.vibecast.data.remote_data.data_source.music.RemoteMusicDataSource
import app.vibecast.data.remote_data.network.music.model.PlaylistApiModel
import app.vibecast.domain.model.SongDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class MusicRepositoryImpl @Inject constructor(
    private val remoteMusicDataSource: RemoteMusicDataSource,
    private val localMusicDataSource: LocalMusicDataSource,
) : MusicRepository {


    override suspend fun deleteSong(song: SongDto) {
        try {
            localMusicDataSource.deleteSong(song)
        } catch (e : Exception){
            Timber.tag("music_db").d("Couldn't delete song")
        }
    }

    override fun getAllSavedSongs(): Flow<List<SongDto>> = flow {
        try {
            localMusicDataSource.getAllSavedSongs().collect {
                emit(it)
            }
        } catch (e: Exception){
            Timber.tag("music_db").d("Couldn't get songs")
        }
    }

    override suspend fun saveSong(song: SongDto) {
        try {
            localMusicDataSource.saveSong(song)
        } catch (e : Exception){
            Timber.tag("music_db").d("Couldn't save song $e")
        }
    }

    override fun getSavedSong(song : SongDto): Flow<SongDto> = flow {
        try {
            localMusicDataSource.getSavedSong(song).collect {
                emit(it)
            }
        } catch (e: Exception){
            Timber.tag("music_db").d("Couldn't get song")
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
    ): Flow<SongDto> = flow {
        try {
            remoteMusicDataSource.getCurrentSong(song, artist, accessCode).collect { result ->
                emit(result.songRespons[0].toSongDto())
            }
        } catch (e: Exception) {
            Timber.tag("Spotify").d(e.localizedMessage ?: "null")
        }
    }
}