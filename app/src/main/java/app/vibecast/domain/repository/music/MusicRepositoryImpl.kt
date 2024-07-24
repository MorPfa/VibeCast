package app.vibecast.domain.repository.music

import app.vibecast.data.local_data.data_source.music.LocalMusicDataSource
import app.vibecast.data.local_data.db.music.model.UserPlaylistEntity
import app.vibecast.data.remote_data.data_source.music.RemoteMusicDataSource
import app.vibecast.data.remote_data.network.music.model.AddToPlaylistPayload
import app.vibecast.data.remote_data.network.music.model.CreatePlaylistPayload
import app.vibecast.data.remote_data.network.music.model.RemoveFromPlaylistPayload
import app.vibecast.domain.model.PlaylistDto
import app.vibecast.domain.model.SelectedPlaylistDto
import app.vibecast.domain.model.SongDto
import app.vibecast.domain.model.TracksDto
import app.vibecast.domain.model.UserDto
import app.vibecast.domain.model.UserPlaylistDto
import app.vibecast.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
    private val remoteDataSource: RemoteMusicDataSource,
    private val localDataSource: LocalMusicDataSource,
) : MusicRepository {


    override suspend fun getSelectedPlaylist(
        id: String,
        accessCode: String,
    ): Resource<SelectedPlaylistDto> {
        return try {
            when (val result = remoteDataSource.getSelectedPlaylist(id, accessCode)) {
                is Resource.Success -> result
                is Resource.Error -> result
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage)
        }
    }

    override suspend fun getUserPlaylists(accessCode: String): Resource<UserPlaylistDto> {
        return try {
            when (val result = remoteDataSource.getUserPlaylists(accessCode)) {
                is Resource.Success -> {
                    val playlistNames =
                        localDataSource.getAllPlaylists().first().map { it.playlistName }
                    val filteredItems = result.data!!.items.filter { it.name in playlistNames }
                    Resource.Success(UserPlaylistDto(items = filteredItems))
                }

                is Resource.Error -> result
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage)
        }
    }

    override suspend fun doesPlaylistExist(cityName: String): Resource<UserPlaylistEntity> {
        val playlists = localDataSource.getAllPlaylists().first()
        Timber.tag("PLAYLIST").d("Playlist: $playlists")
        val hasPlaylist = playlists.any { it.playlistName == cityName }
        return if (hasPlaylist) {
            Resource.Success(playlists.first { it.playlistName == cityName })

        } else {
            Resource.Error("Playlist doesn't exist")
        }
    }

    override suspend fun addSongToPlaylist(
        userId: String,
        playlistName: String,
        accessCode: String,
        data: AddToPlaylistPayload,
    ): Resource<Void> {
        return try {
            when (val playlist = doesPlaylistExist(playlistName)) {
                is Resource.Success -> {
                    Timber.tag("PLAYLIST").d("Playlist exists")
                    when (val result =
                        remoteDataSource.addSongToPlaylist(
                            userId,
                            playlist.data!!.playlistID,
                            accessCode,
                            data
                        )) {
                        is Resource.Success -> result
                        is Resource.Error -> result
                    }
                }

                is Resource.Error -> {
                    when (val newPlaylist = remoteDataSource.createPlaylist(
                        userId,
                        accessCode,
                        CreatePlaylistPayload(playlistName, false, null)
                    )) {
                        is Resource.Success -> {
                            Timber.tag("PLAYLIST").d("Playlist created")
                            localDataSource.savePlaylist(
                                UserPlaylistEntity(
                                    newPlaylist.data!!.id,
                                    playlistName
                                )
                            )
                            when (val result =
                                remoteDataSource.addSongToPlaylist(
                                    userId,
                                    newPlaylist.data.id,
                                    accessCode,
                                    data
                                )) {
                                is Resource.Success -> result
                                is Resource.Error -> result
                            }
                        }

                        is Resource.Error -> {
                            Timber.tag("PLAYLIST")
                                .d("Playlist couldn't be created ${newPlaylist.message}")
                            Resource.Error(null)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage)
        }
    }

    override suspend fun getCurrentUser(accessCode: String): Resource<UserDto> {
        return try {
            when (val result =
                remoteDataSource.getCurrentUser(accessCode)) {
                is Resource.Success -> result
                is Resource.Error -> result
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage)
        }
    }

    override suspend fun deleteSongFromPlaylist(
        playlistId: String,
        accessCode: String,
        data: RemoveFromPlaylistPayload,
    ): Resource<Void> {
        return try {
            when (val result =
                remoteDataSource.deleteSongFromPlaylist(playlistId, accessCode, data)) {
                is Resource.Success -> result
                is Resource.Error -> result
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage)
        }
    }

    override suspend fun deletePlaylist(playlistId: String, accessCode: String): Resource<Void> {
        return try {
            when (val result = remoteDataSource.deletePlaylist(playlistId, accessCode)) {
                is Resource.Success -> result
                is Resource.Error -> result
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage)
        }
    }

    override suspend fun deleteSong(song: SongDto) {
        try {
            localDataSource.deleteSong(song)
        } catch (e: Exception) {
            Timber.tag("music_db").d("Couldn't delete song $e")
        }
    }

    override suspend fun deleteAllSongs() {
        try {
            localDataSource.deleteAllSongs()
        } catch (e: Exception) {
            Timber.tag("music_db").d("Couldn't wipe song table $e")
        }
    }

    override fun getAllSavedSongs(): Flow<List<SongDto>> = flow {
        try {
            localDataSource.getAllSavedSongs().collect {
                emit(it)
            }
        } catch (e: Exception) {
            Timber.tag("music_db").d("Couldn't get songs")
        }
    }

    override suspend fun saveSong(song: SongDto) {
        try {
            localDataSource.saveSong(song)
        } catch (e: Exception) {
            Timber.tag("music_db").d("Couldn't save song $e")
        }
    }

    override fun getSavedSong(song: SongDto): Flow<SongDto> = flow {
        try {
            localDataSource.getSavedSong(song).collect {
                emit(it)
            }
        } catch (e: Exception) {
            Timber.tag("music_db").d("Couldn't get song")
        }
    }

    override suspend fun getPlaylist(category: String, accessCode: String): Resource<PlaylistDto> {
        return try {
            when (val response = remoteDataSource.getPlaylist(category, accessCode)) {
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
            when (val response = remoteDataSource.getCurrentSong(song, artist, accessCode)) {
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