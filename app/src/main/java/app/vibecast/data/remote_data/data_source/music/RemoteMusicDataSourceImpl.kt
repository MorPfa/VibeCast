package app.vibecast.data.remote_data.data_source.music

import app.vibecast.data.remote_data.network.music.api.MusicService
import app.vibecast.data.remote_data.network.music.model.PlaylistApiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class RemoteMusicDataSourceImpl @Inject constructor(
    private val musicService: MusicService,
) : RemoteMusicDataSource {

    val clientSecret = "bd4e7192d0884c73b088eb022a303716"
    override fun getPlaylist(category : String, accessCode : String) : Flow<PlaylistApiModel> = flow {
        try {
            val playlist = musicService.getPlaylist(category, "Bearer $accessCode")
            Timber.tag("Spotify").d( playlist.playlists.items[0].name)
            emit(playlist)
        }
        catch (e : Exception){
            Timber.tag("Spotify").d(e.localizedMessage ?: "null")
        }

    }


}