package app.vibecast.data.remote_data.data_source.music

import app.vibecast.data.remote_data.network.music.api.MusicService
import app.vibecast.data.remote_data.network.music.model.PlaylistApiModel
import app.vibecast.domain.model.PlaylistDto
import app.vibecast.domain.model.TracksDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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

    override fun getPlaylist(category: String, accessCode: String): Flow<PlaylistDto> = flow {
        try {
            val playlist = musicService.getPlaylist(category, "Bearer $accessCode").toDto()
            emit(playlist)
        } catch (e: Exception) {
            Timber.tag("Spotify").d(e.localizedMessage ?: "null")
        }
    }

    override fun getCurrentSong(
        song: String,
        artist: String,
        accessCode: String,
    ): Flow<TracksDto> = flow {
        try {
            val result = musicService.getCurrentSong(
                accessCode = "Bearer $accessCode",
                query = "$song $artist"
            )
            Timber.tag("Spotify").d("result $result")
            emit(TracksDto(result.tracks.href, result.tracks.items[0]))
        } catch (e: Exception) {
            Timber.tag("Spotify").d(e.localizedMessage ?: "null")
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