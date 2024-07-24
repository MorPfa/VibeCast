package app.vibecast.presentation.screens.main_screen.music


import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import app.vibecast.BuildConfig
import app.vibecast.data.remote_data.network.music.model.AddToPlaylistPayload
import app.vibecast.data.remote_data.network.music.model.RemoveFromPlaylistPayload
import app.vibecast.domain.model.SelectedPlaylistDto
import app.vibecast.domain.model.SongDto
import app.vibecast.domain.model.UserDto
import app.vibecast.domain.repository.music.MusicPreferenceRepository
import app.vibecast.domain.repository.music.MusicRepository
import app.vibecast.domain.repository.music.WeatherCondition
import app.vibecast.domain.util.Resource
import app.vibecast.presentation.state.PlayerState
import app.vibecast.presentation.state.PlaylistState
import app.vibecast.presentation.state.SelectedPlaylistState
import app.vibecast.presentation.state.SongState
import app.vibecast.presentation.util.GenreFormatter
import app.vibecast.presentation.util.SaveResult
import com.google.gson.GsonBuilder
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.SpotifyDisconnectedException
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.client.error.RemoteClientException
import com.spotify.protocol.types.PlayerState as SpotifyPlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
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


    private var playerStateSubscription: Subscription<SpotifyPlayerState>? = null
    var spotifyAppRemote: SpotifyAppRemote? = null
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val clientId = BuildConfig.SPOTIFY_KEY
    private val redirectUri = "vibecast://callback"
    private val genreFormatter = GenreFormatter()


    private var _curPlaylistState = MutableLiveData<SelectedPlaylistState>()
    val curPlaylistState: LiveData<SelectedPlaylistState> get() = _curPlaylistState


    private var _isSongSaved = MutableLiveData(false)
    val isSongSaved: LiveData<Boolean> get() = _isSongSaved

    private var _saveResult = MutableLiveData<SaveResult>()
    val saveResult: LiveData<SaveResult> get() = _saveResult


    private var _curUser = MutableStateFlow<UserDto?>(null)
    private val curUser: StateFlow<UserDto?> get() = _curUser


    fun getSelectedPlaylist(id : String){
        viewModelScope.launch(Dispatchers.IO) {
            when(val result = musicRepository.getSelectedPlaylist(id, token.value!!)){
                is Resource.Success -> setCurPlaylist(SelectedPlaylistState(data = result.data))
                is Resource.Error -> setCurPlaylist(SelectedPlaylistState(error = result.message))
            }
        }
    }
    private suspend fun setCurPlaylist(playlistState : SelectedPlaylistState){
        withContext(Dispatchers.Main){
            _curPlaylistState.value = playlistState
        }

    }
    fun addSongToPlaylist(
        playlistName: String,
        data: AddToPlaylistPayload,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = musicRepository.addSongToPlaylist(
                curUser.value!!.id,
                playlistName,
                token.value!!,
                data
            )) {
                is Resource.Success -> {
                    withContext(Dispatchers.Main) {
                        _saveResult.value = SaveResult(success = true, playlistName = playlistName)
                    }
                }

                is Resource.Error -> {
                    withContext(Dispatchers.Main) {
                        _saveResult.value = SaveResult(success = false, error = result.message)
                    }
                }
            }
        }
    }

    suspend fun deleteSongFromPlaylist(
        playlistId: String,
        data: RemoveFromPlaylistPayload,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result =
                musicRepository.deleteSongFromPlaylist(playlistId, token.value!!, data)) {
                is Resource.Success -> {
                    withContext(Dispatchers.Main) {
                        _saveResult.value = SaveResult(success = true, playlistName = playlistId)
                    }
                }

                is Resource.Error -> {
                    withContext(Dispatchers.Main) {
                        _saveResult.value = SaveResult(success = false, error = result.message)
                    }
                }
            }
        }
    }

    suspend fun deletePlaylist(playlistId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = musicRepository.deletePlaylist(playlistId, token.value!!)) {
                is Resource.Success -> {
                    withContext(Dispatchers.Main) {
                        _saveResult.value = SaveResult(success = true, playlistName = playlistId)
                    }
                }

                is Resource.Error -> {
                    withContext(Dispatchers.Main) {
                        _saveResult.value = SaveResult(success = false, error = result.message)
                    }
                }
            }
        }
    }

    private fun updateSavedSongStatus(trackUri: String) {
//         Timber.tag("music_db").d("curr track $trackUri ${savedSongs.value}")
        val containsSong = savedSongs.value?.any { song ->
            song.trackUri == trackUri
        }
//         Timber.tag("music_db").d(containsSong.toString())
        _isSongSaved.value = containsSong
    }

    val savedSongs: LiveData<List<SongDto>> = musicRepository.getAllSavedSongs().map { songs ->
        songs.map { song ->
            song
        }
    }.asLiveData()

    private var _playlistState = MutableLiveData<PlaylistState>()
    val playlistState: LiveData<PlaylistState> get() = _playlistState

    fun setupSelectedPlaylist(playlist: SelectedPlaylistDto){
        assertAppRemoteConnected().let {
            it.playerApi.play(playlist.uri).setResultCallback { _ ->
                it.playerApi
                    .pause()
                    .setResultCallback {
                        logMessage("pause")
                    }
                    .setErrorCallback(errorCallback)
            }
        }
    }

    fun getUserPlaylists() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = musicRepository.getUserPlaylists(token.value!!)) {
                is Resource.Success -> {
                    withContext(Dispatchers.Main) {
                        _playlistState.value = PlaylistState(data = result.data)
                    }
                }

                is Resource.Error -> {
                    withContext(Dispatchers.Main) {
                        _playlistState.value = PlaylistState(error = result.message)
                    }
                }
            }
        }
    }

    private var _currentSong = MutableLiveData<SongState>()
    val currentSong: LiveData<SongState> get() = _currentSong

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
    var token = _token


    fun getCurrentSong(song: String, artist: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = musicRepository.getCurrentSong(song, artist, token.value!!)) {
                is Resource.Success -> {
                    result.data?.let {
                        Timber.tag("music_db").d("result ${result.data.items.externalUrls.spotify}")
                        withContext(Dispatchers.Main) {
                            _currentSong.value = SongState(song = result.data.items)
                        }
                    }
                }

                is Resource.Error -> {
                    withContext(Dispatchers.Main) {
                        _currentSong.value = SongState(error = "Couldn't get song info")
                    }

                }
            }
        }
    }

    fun getPlaylist(weather: String) {
        try {
            viewModelScope.launch {
                val genre = getGenre(weather)
                when (val result = musicRepository.getPlaylist(genre, token.value!!)) {
                    is Resource.Success -> {
                        Timber.tag("Spotify")
                            .d("Playlist name : ${result.data?.items?.get(0)?.name}")
                        result.data?.let { playlists ->
                            val index = playlists.items.indices.random()
                            Timber.tag("Spotify")
                                .d("Playlist name : ${playlists.items[index].name}")
                            val lofiPlaylist =
                                playlists.items.find { it.name.contains("lofi", ignoreCase = true) }
                            if (lofiPlaylist != null) {
                                Timber.tag("Spotify").d("Lofi playlist found: ${lofiPlaylist.name}")
                                playPlaylist(lofiPlaylist.uri)

                            } else {
                                Timber.tag("Spotify").d("No lofi playlist found")
                                playPlaylist(playlists.items[index].uri)

                            }
                        }
                    }

                    is Resource.Error -> {

                    }
                }
            }
        } catch (e: Exception) {
            Timber.tag("Spotify").d(e.localizedMessage ?: "null")
        }
    }

    fun deleteSong(song: SongDto) {
        viewModelScope.launch(Dispatchers.IO) {
            musicRepository.deleteSong(song)
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
                    getCurrentUser()
                    spotifyAppRemote?.let {
                        it.userApi.capabilities.setResultCallback { capabilities ->
                            Timber.tag("Spotify").d("Capabilities $capabilities")
                            _userCapabilitiesState.value = capabilities.canPlayOnDemand
                        }
                    }


                }

                override fun onFailure(throwable: Throwable) {
                    Timber.tag("Spotify").e(throwable)
                }
            })
    }


    fun getCurrentUser() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = musicRepository.getCurrentUser(token.value!!)) {
                is Resource.Success -> _curUser.value = result.data!!
                is Resource.Error -> Timber.tag("Spotify").e(result.message)
            }
        }
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
            } as Subscription<SpotifyPlayerState>
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
        fun onPlayerStateUpdated(playerState: SpotifyPlayerState)
    }


    private var playerStateListener: PlayerStateListener? = null


    private var _userCapabilitiesState = MutableLiveData<Boolean>()

    val userCapabilitiesState: LiveData<Boolean> get() = _userCapabilitiesState

    private var _playerState = MutableLiveData<PlayerState>()

    val playerState: LiveData<PlayerState> get() = _playerState


    private val playerStateEventCallback =
        Subscription.EventCallback<SpotifyPlayerState> { playerState ->
            playerStateListener?.onPlayerStateUpdated(playerState)
            _playerState.value = PlayerState(state = playerState)
            updateSavedSongStatus(playerState.track.uri)
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

    private val errorCallback = { throwable: Throwable ->
        if (spotifyAppRemote?.isConnected == false) {
            connectToSpotify(token.value!!)
        }
        if (throwable is RemoteClientException) {
            logError(throwable)
        } else {
            logError(throwable)
        }
    }

    private fun logError(throwable: Throwable) {
        Timber.tag("Spotify").e(throwable)
    }

    private fun logMessage(msg: String) {
        Timber.tag("Spotify").e(msg)
    }
}




