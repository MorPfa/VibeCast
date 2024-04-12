package app.vibecast.data.remote_data.data_source.music

import app.vibecast.data.remote_data.network.music.api.MusicService
import app.vibecast.data.remote_data.network.music.model.PlaylistApiModel
import app.vibecast.data.remote_data.network.music.model.SearchModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class RemoteMusicDataSourceImpl @Inject constructor(
    private val musicService: MusicService,
) : RemoteMusicDataSource {

    override fun getPlaylist(category : String, accessCode : String) : Flow<PlaylistApiModel> = flow {
        try {
            val playlist = musicService.getPlaylist(category, "Bearer $accessCode")
            emit(playlist)
        }
        catch (e : Exception){
            Timber.tag("Spotify").d(e.localizedMessage ?: "null")
        }
    }

    override fun getCurrentSong(song: String, artist: String, accessCode : String): Flow<SearchModel> = flow {
        try {
            val songResult = musicService.getCurrentSong("Bearer $accessCode", song, artist)
            emit(songResult)
        } catch (e : Exception){
            Timber.tag("Spotify").d(e.localizedMessage ?: "null")
        }
    }
}