package app.vibecast.presentation.music

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.vibecast.presentation.TAG
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.SpotifyDisconnectedException
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.PlayerContext
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track


class MusicViewModel(private val appRemote: SpotifyAppRemote?) : ViewModel() {

    private var playerStateSubscription: Subscription<PlayerState>? = null
    private var playerContextSubscription: Subscription<PlayerContext>? = null

    fun connected() {
        appRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback {
            val track: Track = it.track
            Log.d("MainActivity", track.name + " by " + track.artist.name)
            subscribeToPlayerContext()
            subscribeToPlayerState()
        }
    }

    fun subscribeToPlayerContext() {
        playerContextSubscription = cancelAndResetSubscription(playerContextSubscription)
        playerContextSubscription = assertAppRemoteConnected()
            .playerApi
            .subscribeToPlayerContext()
//            .setEventCallback(playerContextEventCallback)
            .setErrorCallback { throwable ->
                logError(throwable)
            } as Subscription<PlayerContext>
    }

    fun onSkipNextButtonClicked() {
        assertAppRemoteConnected()
            .playerApi
            .skipNext()
            .setResultCallback { logMessage( "skip next") }
            .setErrorCallback(errorCallback)
    }
    fun onSkipPreviousButtonClicked() {
        assertAppRemoteConnected()
            .playerApi
            .skipPrevious()
            .setResultCallback { logMessage("skip previous") }
            .setErrorCallback(errorCallback)
    }

    fun subscribeToPlayerState() {
        playerStateSubscription = cancelAndResetSubscription(playerStateSubscription)
        playerStateSubscription = assertAppRemoteConnected()
            .playerApi
            .subscribeToPlayerState()
//            .setEventCallback(playerStateEventCallback)
            .setLifecycleCallback(
                object : Subscription.LifecycleCallback {
                    override fun onStart() {
                        logMessage("Event: start")
                    }

                    override fun onStop() {
                        logMessage("Event: end")
                    }
                })
            .setErrorCallback {
            } as Subscription<PlayerState>
    }

//    private val playerContextEventCallback = Subscription.EventCallback<PlayerContext> { playerContext ->
//        binding.currentContextLabel.apply {
//            text = String.format(Locale.US, "%s\n%s", playerContext.title, playerContext.subtitle)
//            tag = playerContext
//        }
//    }
    private fun <T : Any?> cancelAndResetSubscription(subscription: Subscription<T>?): Subscription<T>? {
        return subscription?.let {
            if (!it.isCanceled) {
                it.cancel()
            }
            null
        }
    }

    private fun assertAppRemoteConnected(): SpotifyAppRemote {
        appRemote?.let {
            if (it.isConnected) {
                return it
            }
        }
        throw SpotifyDisconnectedException()
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
                        .setErrorCallback(errorCallback)
                } else {
                    appRemote
                        .playerApi
                        .pause()
                        .setResultCallback {
                            logMessage("pause")
                            _isPlaying.postValue(false)
                        }
                        .setErrorCallback(errorCallback)
                }

            }
    }

    private val errorCallback = { throwable: Throwable -> logError(throwable) }
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
