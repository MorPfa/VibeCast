package app.vibecast.data.remote_data.data_source.music

import app.vibecast.data.remote_data.network.music.api.MusicService
import app.vibecast.data.remote_data.network.music.model.PlaylistApiModel
import app.vibecast.domain.model.PlaylistDto
import app.vibecast.domain.model.TracksDto
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