package app.vibecast.presentation.screens.main_screen.music


import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.vibecast.BuildConfig
import app.vibecast.domain.repository.music.MusicPreferenceRepository
import app.vibecast.domain.repository.music.MusicRepository
import app.vibecast.domain.repository.music.WeatherCondition
import app.vibecast.presentation.util.GenreFormatter
import com.google.gson.GsonBuilder
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.SpotifyDisconnectedException
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MusicViewModel @Inject constructor(
    private val application: Application,
    private val musicRepository: MusicRepository,
    private val musicPreferenceRepository: MusicPreferenceRepository,
) : ViewModel() {


    private var playerStateSubscription: Subscription<PlayerState>? = null
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val clientId = BuildConfig.SPOTIFY_KEY
    private val redirectUri = "vibecast://callback"
    private val genreFormatter = GenreFormatter()


    private suspend fun getGenre(weather: String): String {
        val category = when (weather) {
            "Thunderstorm" -> WeatherCondition.STORMY
            "Drizzle" -> WeatherCondition.RAINY
            "Rain" -> WeatherCondition.RAINY
            "Snow" -> WeatherCondition.SNOWY
            "Mist" -> WeatherCondition.CLOUDY
            "Fog" -> WeatherCondition.FOGGY
            "Tornado" -> WeatherCondition.STORMY
            "Dust" -> WeatherCondition.FOGGY
            "Clear" -> WeatherCondition.SUNNY
            "Clouds" -> WeatherCondition.CLOUDY
            else -> WeatherCondition.CLOUDY
        }
        return withContext(Dispatchers.IO) {
            val genre = musicPreferenceRepository.getPreference(category)
            Timber.tag("Spotify").d("Unformatted genre: $genre")
            genreFormatter.formatQuery(genre)

        }
    }

    private val _token = MutableLiveData<String>()
    private var token = _token


    fun getPlaylist(weather: String) {
        try {
            viewModelScope.launch {
                val genre = getGenre(weather)
                Timber.tag("Spotify").d("Formatted genre : $genre")
                musicRepository.getPlaylist(genre, token.value!!).collect {result ->
                    val playlists = result.playlists.items
                    Timber.tag("Spotify").d("Weather: $weather")
                    val index = playlists.indices.random()
                    Timber.tag("Spotify").d("Playlist name : ${playlists[index].name}")
                    val lofiPlaylist = playlists.find { it.name.contains("lofi", ignoreCase = true) }
                    if (lofiPlaylist != null) {
                        Timber.tag("Spotify").d("Lofi playlist found: ${lofiPlaylist.name}")
                        playPlaylist(lofiPlaylist.uri)

                    } else {
                        Timber.tag("Spotify").d("No lofi playlist found")
                        playPlaylist(playlists[index].uri)

                    }

                }
            }

        } catch (e: Exception) {
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

    fun connectToSpotify(token: String) {
        val connectionParams = ConnectionParams.Builder(clientId)
            .setRedirectUri(redirectUri)
            .build()
        SpotifyAppRemote.connect(
            application.applicationContext,
            connectionParams,
            object : Connector.ConnectionListener {
                override fun onConnected(appRemote: SpotifyAppRemote) {
                    spotifyAppRemote = appRemote
                    Timber.tag("Spotify").d("Connected! Yay!")
                    subscribeToPlayerState()
                    _token.value = token

                }

                override fun onFailure(throwable: Throwable) {
                    Timber.tag("Spotify").e(throwable)

                }
            })
    }


    fun disconnectFromSpotify() {
        SpotifyAppRemote.disconnect(spotifyAppRemote)
    }


    private fun playPlaylist(uri: String) {
        Timber.tag("Spotify").d(uri)
        assertAppRemoteConnected().let {
           it.playerApi.play(uri).setResultCallback { _ ->
                    it.playerApi
                        .pause()
                        .setResultCallback {
                            logMessage("pause")
                        }
                        .setErrorCallback(errorCallback)
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
//        Timber.tag("Spotify").v("Player State: %s", gson.toJson(playerState))
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
        Timber.tag("Spotify").e(throwable)
    }

    private fun logMessage(msg: String) {
        Timber.tag("Spotify").e(msg)
    }
}




