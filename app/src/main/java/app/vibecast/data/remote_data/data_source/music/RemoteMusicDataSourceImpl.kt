package app.vibecast.data.remote_data.data_source.music

import app.vibecast.data.remote_data.data_source.music.util.FIELDS
import app.vibecast.data.remote_data.network.music.api.MusicService
import app.vibecast.data.remote_data.network.music.model.AddToPlaylistPayload
import app.vibecast.data.remote_data.network.music.model.CreatePlaylistPayload
import app.vibecast.data.remote_data.network.music.model.PlaylistApiModel
import app.vibecast.data.remote_data.network.music.model.PlaylistCreated
import app.vibecast.data.remote_data.network.music.model.RemoveFromPlaylistPayload
import app.vibecast.data.remote_data.network.music.model.UserPlaylist
import app.vibecast.data.remote_data.network.music.model.UserPlaylists
import app.vibecast.domain.model.PlaylistCreatedDto
import app.vibecast.domain.model.PlaylistDto
import app.vibecast.domain.model.SelectedPlaylistDto
import app.vibecast.domain.model.TracksDto
import app.vibecast.domain.model.UserDto
import app.vibecast.domain.model.UserPlaylistDto
import app.vibecast.domain.util.Resource
import timber.log.Timber
import javax.inject.Inject


/**
 * Implementation of [RemoteMusicDataSource]
 *
 * Methods:
 * - [getPlaylist] Fetches playlist from API and returns Playlist DTO
 * - [getCurrentSong] Fetches info for currently playing song from API and returns Track Data Transfer Object
 * - [toDto] Converts API response Model Playlist Data Transfer Object.
 */
class RemoteMusicDataSourceImpl @Inject constructor(
    private val musicService: MusicService,
) : RemoteMusicDataSource {


    override suspend fun getSelectedPlaylist(
        id: String,
        accessCode: String,
    ): Resource<SelectedPlaylistDto> {
        return try {
            val response = musicService.getPlaylistById(
                playlistId = id,
                accessCode = "Bearer $accessCode",
                market = null,
                fields = FIELDS
            )
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {

                    Resource.Success(data = responseBody.toDto())
                } else {
                    Resource.Error(message = "Response body is null")
                }

            } else {
                Resource.Error(message = response.message())
            }

        } catch (e: Exception) {
            Resource.Error(e.localizedMessage)
        }
    }

    override suspend fun getPlaylist(category: String, accessCode: String): Resource<PlaylistDto> {
        return try {
            val response = musicService.getPlaylist(category, "Bearer $accessCode")
            if (response.isSuccessful) {
                val playlist = response.body()
                if (playlist != null) {
                    Resource.Success(playlist.toDto())
                } else {
                    Resource.Error("Playlist is null")
                }
            } else {
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Timber.tag("Spotify").d(e.localizedMessage ?: "null")
            Resource.Error(e.localizedMessage ?: "Unknown error")
        }
    }

    override suspend fun getCurrentSong(
        song: String,
        artist: String,
        accessCode: String,
    ): Resource<TracksDto> {
        return try {
            val response = musicService.getCurrentSong(
                accessCode = "Bearer $accessCode",
                query = "$song $artist"
            )
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    Timber.tag("Spotify").d("response $responseBody")
                    Resource.Success(
                        TracksDto(
                            responseBody.tracks.href,
                            responseBody.tracks.items[0]
                        )
                    )
                } else {
                    Resource.Error("Response body is null")
                }
            } else {
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Timber.tag("Spotify").d(e.localizedMessage ?: "null")
            Resource.Error(e.localizedMessage ?: "Unknown error")
        }
    }

    override suspend fun doesPlaylistExist(
        playlistId: String,
        accessCode: String,
    ): Boolean {
        return try {
            val response = musicService.doesPlaylistExist(
                playlistId = playlistId,
                accessCode = "Bearer $accessCode",
            )
            if (response.isSuccessful) {
                val doesExist = response.body()?.playlistExists?.get(0) ?: false
                doesExist
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getUserPlaylists(accessCode: String): Resource<UserPlaylistDto> {
        return try {
            val response = musicService.getUserPlaylists(
                accessCode = "Bearer $accessCode"
            )
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    Resource.Success(data = responseBody.toDto())
                } else {
                    Resource.Error("Response body is null")
                }
            } else {
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Unknown error")
        }
    }

    override suspend fun createPlaylist(
        userId: String,
        accessCode: String,
        data: CreatePlaylistPayload,
    ): Resource<PlaylistCreatedDto> {
        return try {
            val response = musicService.createPlaylist(
                userId = userId,
                accessCode = "Bearer $accessCode",
                data = data
            )
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    Timber.tag("PLAYLIST").d("Success $responseBody")
                    Resource.Success(data = responseBody.toDto())
                } else {
                    Resource.Error("Response body is null")
                }
            } else {
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Unknown error")
        }
    }


    override suspend fun getCurrentUser(accessCode: String): Resource<UserDto> {
        return try {
            val response = musicService.getCurrentUser(accessCode = "Bearer $accessCode")
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    Resource.Success(data = responseBody.let { data ->
                        UserDto(
                            data.id,
                            data.name,
                            data.uri,
                        )
                    })
                } else {
                    Resource.Error("Response body is null")
                }
            } else {
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Unknown error")
        }
    }

    override suspend fun addSongToPlaylist(
        userId: String,
        playlistId: String,
        accessCode: String,
        data: AddToPlaylistPayload,
    ): Resource<Void> {
        return try {
            val response = musicService.addSongToPlaylist(
                playlistId = playlistId,
                accessCode = "Bearer $accessCode",
                data = data
            )
            if (response.isSuccessful) {
                Resource.Success(data = null)
            } else {
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Unknown error")
        }
    }

    override suspend fun deleteSongFromPlaylist(
        playlistId: String,
        accessCode: String,
        data: RemoveFromPlaylistPayload,
    ): Resource<Void> {
        return try {
            val response = musicService.deleteSongFromPlaylist(
                playlistId = playlistId,
                accessCode = "Bearer $accessCode",
                data = data
            )
            if (response.isSuccessful) {
                Resource.Success(data = null)
            } else {
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Unknown error")
        }
    }

    override suspend fun deletePlaylist(playlistId: String, accessCode: String): Resource<Void> {
        return try {
            val response = musicService.deletePlaylist(
                playlistId = playlistId,
                accessCode = "Bearer $accessCode",
            )
            if (response.isSuccessful) {
                Resource.Success(data = null)
            } else {
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Unknown error")
        }
    }


    private fun UserPlaylist.toDto(): SelectedPlaylistDto {
        return SelectedPlaylistDto(
            description = this.description,
            href = this.href,
            id = this.id,
            name = this.name,
            owner = this.owner,
            public = this.public,
            snapshotId = this.snapshotId,
            tracks = this.tracks,
            uri = this.uri
        )
    }

    private fun UserPlaylists.toDto(): UserPlaylistDto {
        return UserPlaylistDto(
            items = this.items
        )
    }

    private fun PlaylistCreated.toDto(): PlaylistCreatedDto {
        return PlaylistCreatedDto(
            href = this.href,
            id = this.id,
            name = this.name,
            owner = this.owner,
            public = this.public,
            snapshotId = this.snapshotId,
            tracks = this.tracks,
            uri = this.uri
        )
    }

    private fun PlaylistApiModel.toDto(): PlaylistDto {
        return PlaylistDto(
            href = this.playlists.href,
            items = this.playlists.items,
            limit = this.playlists.limit,
            next = this.playlists.next,
            offset = this.playlists.offset,
            total = this.playlists.total
        )
    }
}