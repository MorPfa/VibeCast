package app.vibecast.domain.repository.music

import app.vibecast.data.local_data.data_source.music.LocalMusicDataSource
import app.vibecast.data.remote_data.data_source.music.RemoteMusicDataSource
import app.vibecast.domain.model.PlaylistDto
import app.vibecast.domain.model.SongDto
import app.vibecast.domain.model.TracksDto
import app.vibecast.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject


/**
 * Implementation of [MusicRepository]
 *
 * Methods:
 * - [deleteSong] Deletes saved song from database
 * - [saveSong] Saves song to database
 * - [getAllSavedSongs] Gets all saved songs from database
 * - [getCurrentSong] Fetches info for currently playing song
 * - [getPlaylist] Fetches playlist to queue when app is launched
 */

class MusicRepositoryImpl @Inject constructor(
    private val remoteMusicDataSource: RemoteMusicDataSource,
    private val localMusicDataSource: LocalMusicDataSource,
) : MusicRepository {


    override suspend fun deleteSong(song: SongDto) {
        try {
            localMusicDataSource.deleteSong(song)
        } catch (e: Exception) {
            Timber.tag("music_db").d("Couldn't delete song $e")
        }
    }

    override suspend fun deleteAllSongs() {
        try {
            localMusicDataSource.deleteAllSongs()
        } catch (e: Exception) {
            Timber.tag("music_db").d("Couldn't wipe song table $e")
        }
    }

    override fun getAllSavedSongs(): Flow<List<SongDto>> = flow {
        try {
            localMusicDataSource.getAllSavedSongs().collect {
                emit(it)
            }
        } catch (e: Exception) {
            Timber.tag("music_db").d("Couldn't get songs")
        }
    }

    override suspend fun saveSong(song: SongDto) {
        try {
            localMusicDataSource.saveSong(song)
        } catch (e: Exception) {
            Timber.tag("music_db").d("Couldn't save song $e")
        }
    }

    override fun getSavedSong(song: SongDto): Flow<SongDto> = flow {
        try {
            localMusicDataSource.getSavedSong(song).collect {
                emit(it)
            }
        } catch (e: Exception) {
            Timber.tag("music_db").d("Couldn't get song")
        }
    }

    override suspend fun getPlaylist(category: String, accessCode: String): Resource<PlaylistDto> {
        return try {
            when (val response = remoteMusicDataSource.getPlaylist(category, accessCode)) {
                is Resource.Success -> {
                    Resource.Success(response.data!!)
                }

                is Resource.Error -> {
                    Resource.Error(response.message)
                }
            }
        } catch (e: Exception) {
            Timber.tag("Spotify").d(e.localizedMessage ?: "null")
            Resource.Error(e.localizedMessage)
        }
    }


    override suspend fun getCurrentSong(
        song: String,
        artist: String,
        accessCode: String,
    ): Resource<TracksDto> {
        return try {
            when (val response = remoteMusicDataSource.getCurrentSong(song, artist, accessCode)) {
                is Resource.Success -> {
                    Resource.Success(response.data!!)
                }

                is Resource.Error -> {
                    Resource.Error(response.message)
                }
            }
        } catch (e: Exception) {
            Timber.tag("Spotify").d(e.localizedMessage ?: "null")
            Resource.Error(e.localizedMessage)
        }
    }
}