package app.vibecast.presentation.state

import com.spotify.protocol.types.PlayerState as SpotifyPlayerState

data class PlayerState(
    val error : String? = null,
    val state: SpotifyPlayerState
)
