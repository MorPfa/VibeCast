package app.vibecast.presentation.state

import app.vibecast.domain.model.UserPlaylistDto

data class PlaylistState (
    val data : UserPlaylistDto? = null,
    val error : String? = null
)