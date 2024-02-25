package app.vibecast.data.remote.source

import android.util.Log
import app.vibecast.data.TAGS.MUSIC_ERROR
import app.vibecast.data.data_repository.data_source.remote.RemoteMusicDataSource
import app.vibecast.data.remote.network.music.MusicService
import app.vibecast.data.remote.network.music.PlaylistApiModel
import app.vibecast.presentation.TAG
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
            val playlist = musicService.getPlaylist(category, accessCode)
            Timber.tag("Spotify").d( playlist.playlists.items[0].name)
            emit(playlist)
        }
        catch (e : Exception){
            Timber.tag("Spotify").d(e.localizedMessage ?: "null")
        }

    }


}