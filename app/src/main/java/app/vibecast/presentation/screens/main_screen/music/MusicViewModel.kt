package app.vibecast.presentation.screens.main_screen.music

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.vibecast.BuildConfig
import app.vibecast.data.remote_data.network.music.model.PlaylistApiModel
import app.vibecast.domain.repository.MusicRepository
import app.vibecast.presentation.TAG
import com.google.gson.GsonBuilder
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.SpotifyDisconnectedException
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.PlayerContext
import com.spotify.protocol.types.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MusicViewModel @Inject constructor(
    private val application: Application,
    private val musicRepository: MusicRepository,
) : ViewModel() {


    private var playerStateSubscription: Subscription<PlayerState>? = null
    private var playerContextSubscription: Subscription<PlayerContext>? = null
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val clientId = BuildConfig.SPOTIFY_KEY
    private val redirectUri = "vibecast://callback"
    private var started : Boolean = false

    private val _coverImage =MutableLiveData<String>()
    var coverImage : LiveData<String> = _coverImage


    fun getPlaylist(category: String, accessCode : String): Flow<PlaylistApiModel> = flow {
        try {
            Timber.tag("Spotify").d("getPlaylist called")
            musicRepository.getPlaylist(category, accessCode).collect {
                Timber.tag("Spotify").d(it.playlists.items[0].name)
                emit(it)
            }
        } catch (e : Exception){
            Timber.tag("Spotify").d(e.localizedMessage ?: "null")
        }
    }


    fun setRepeatStatus() {
        assertAppRemoteConnected()
            .playerApi
            .toggleRepeat()
            .setResultCallback { logMessage("toggle repeat") }
            .setErrorCallback(errorCallback)
    }


    fun setShuffleStatus() {
        assertAppRemoteConnected().let {
            it.playerApi
                .playerState
                .setResultCallback { playerState ->
                    if (playerState.playbackOptions.isShuffling) {
                        it.playerApi
                            .setShuffle(false)
                            .setResultCallback { logMessage("unshuffled") }
                            .setErrorCallback(errorCallback)
                    } else {
                        it.playerApi
                            .setShuffle(true)
                            .setResultCallback {
                                logMessage("shuffled")
                            }
                            .setErrorCallback(errorCallback)
                    }
                }
        }
    }

    fun seekTo(seekToPosition: Long) {
        assertAppRemoteConnected()
            .playerApi
            .seekTo(seekToPosition)
            .setErrorCallback(errorCallback)
    }

    fun connectToSpotify(acessToken : String) {
        val connectionParams = ConnectionParams.Builder(clientId)
            .setRedirectUri(redirectUri)
            .build()
        SpotifyAppRemote.connect(
            application.applicationContext,
            connectionParams,
            object : Connector.ConnectionListener {
                override fun onConnected(appRemote: SpotifyAppRemote) {
                    spotifyAppRemote = appRemote
                    Log.d("MainActivity", "Connected! Yay!")
                    // Now you can start interacting with App Remote
                    //            subscribeToPlayerContext()
                    subscribeToPlayerState()
                    viewModelScope.launch {
                        getPlaylist("jazz", acessToken).collect{
                            playPlaylist(it.playlists.items[0].uri)
                        }
                    }

                }

                override fun onFailure(throwable: Throwable) {
                    Log.e("MainActivity", throwable.message, throwable)
                    // Something went wrong when attempting to connect! Handle errors here
                }
            })
    }


    fun disconnectFromSpotify() {
        SpotifyAppRemote.disconnect(spotifyAppRemote)
    }

    fun playPlaylist(uri : String){
        assertAppRemoteConnected().let {
            if (!started){
                it.playerApi.play(uri)
                it.playerApi.pause()
            }
            else {
                it.playerApi.resume()
            }

        }
    }

    fun onPlayPauseButtonClicked() {
        assertAppRemoteConnected().let {
            it.playerApi
                .playerState
                .setResultCallback { playerState ->
                    if (playerState.isPaused) {
                        it.playerApi
                            .resume()
                            .setResultCallback { logMessage("play") }
                            .setErrorCallback(errorCallback)
                    } else {
                        it.playerApi
                            .pause()
                            .setResultCallback {
                                logMessage("pause")
                            }
                            .setErrorCallback(errorCallback)
                    }
                }
        }

    }
//
//    private fun subscribeToPlayerContext() {
//        playerContextSubscription = cancelAndResetSubscription(playerContextSubscription)
//        playerContextSubscription = assertAppRemoteConnected()
//            .playerApi
//            .subscribeToPlayerContext()
////            .setEventCallback(playerContextEventCallback)
//            .setErrorCallback { throwable ->
//                logError(throwable)
//            } as Subscription<PlayerContext>
//    }

    fun onSkipNextButtonClicked() {
        assertAppRemoteConnected()
            .playerApi
            .skipNext()
            .setResultCallback { logMessage("skip next") }
            .setErrorCallback(errorCallback)
    }

    fun onSkipPreviousButtonClicked() {
        assertAppRemoteConnected()
            .playerApi
            .skipPrevious()
            .setResultCallback { logMessage("skip previous") }
            .setErrorCallback(errorCallback)
    }

    private fun subscribeToPlayerState() {
        playerStateSubscription = cancelAndResetSubscription(playerStateSubscription)
        playerStateSubscription = assertAppRemoteConnected()
            .playerApi
            .subscribeToPlayerState()
            .setEventCallback(playerStateEventCallback)
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

    fun assertAppRemoteConnected(): SpotifyAppRemote {
        spotifyAppRemote.let {
            if (it?.isConnected == true) {
                return it
            }
        }
        throw SpotifyDisconnectedException()
    }


    interface PlayerStateListener {
        fun onPlayerStateUpdated(playerState: PlayerState)
    }

    private var playerStateListener: PlayerStateListener? = null

    fun setPlayerStateListener(listener: PlayerStateListener) {
        playerStateListener = listener
    }

    private val playerStateEventCallback = Subscription.EventCallback<PlayerState> { playerState ->
        playerStateListener?.onPlayerStateUpdated(playerState)
        Log.v(TAG, String.format("Player State: %s", gson.toJson(playerState)))
    }


    override fun onCleared() {
        super.onCleared()
        spotifyAppRemote.let { remote ->
            if (remote?.isConnected == true) {
                SpotifyAppRemote.disconnect(remote)
            }
        }
    }


    private val errorCallback = { throwable: Throwable -> logError(throwable) }
    private fun logError(throwable: Throwable) {
        Log.e(TAG, "", throwable)
    }

    private fun logMessage(msg: String) {
        Log.e(TAG, msg)
    }
}




