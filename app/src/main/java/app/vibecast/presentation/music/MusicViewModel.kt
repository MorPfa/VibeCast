package app.vibecast.presentation.music

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.vibecast.BuildConfig
import app.vibecast.presentation.TAG
import com.google.gson.GsonBuilder
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.SpotifyDisconnectedException
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.PlayerContext
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track
import kotlinx.coroutines.launch


class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>()

    private var playerStateSubscription: Subscription<PlayerState>? = null
    private var playerContextSubscription: Subscription<PlayerContext>? = null
    private var spotifyAppRemote: SpotifyAppRemote? = null


    private val clientId = BuildConfig.SPOTIFY_KEY

    private val redirectUri = "vibecast://callback"



    fun connectToSpotify(){
        val connectionParams = ConnectionParams.Builder(clientId)
            .setRedirectUri(redirectUri)
            .showAuthView(true)
            .build()
        SpotifyAppRemote.connect(
            context.applicationContext,
            connectionParams,
            object : Connector.ConnectionListener {
                override fun onConnected(appRemote: SpotifyAppRemote) {
                    spotifyAppRemote = appRemote
                    Log.d("MainActivity", "Connected! Yay!")
                    // Now you can start interacting with App Remote
                    connected()
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e("MainActivity", throwable.message, throwable)
                    // Something went wrong when attempting to connect! Handle errors here
                }
            })



    }

    fun disconnectFromSpotify(){
        SpotifyAppRemote.disconnect(spotifyAppRemote)
    }

    private fun connected() {
//            subscribeToPlayerContext()
            subscribeToPlayerState()

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

    private fun assertAppRemoteConnected(): SpotifyAppRemote {
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


    private val gson = GsonBuilder().setPrettyPrinting().create()



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




