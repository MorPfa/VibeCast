package app.vibecast.presentation.music

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.vibecast.presentation.TAG
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track


class MusicViewModel(private val appRemote: SpotifyAppRemote?) : ViewModel() {



    fun connected() {
        appRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback {
            val track: Track = it.track
            Log.d("MainActivity", track.name + " by " + track.artist.name)
        }


    }

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> = _isPlaying

    fun onPlayPauseButtonClicked() {
        appRemote
            ?.playerApi
            ?.playerState
            ?.setResultCallback { playerState: PlayerState ->
                if (playerState.isPaused) {
                    appRemote
                        .playerApi
                        .resume()
                        .setResultCallback {
                            logMessage("play")
                            _isPlaying.postValue(true)
                        }
                        .setErrorCallback(mErrorCallback)
                } else {
                    appRemote
                        .playerApi
                        .pause()
                        .setResultCallback {
                            logMessage("pause")
                            _isPlaying.postValue(false)
                        }
                        .setErrorCallback(mErrorCallback)
                }

            }
    }

    private val mErrorCallback: (Throwable) -> Unit = this::logError
    private fun logError(throwable: Throwable) {
        Log.e(TAG, "", throwable)
    }
    private fun logMessage(msg: String) {
        Log.e(TAG, msg )
    }
}



class MusicViewModelFactory(private val spotifyAppRemote: SpotifyAppRemote?) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
            return MusicViewModel(spotifyAppRemote) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
