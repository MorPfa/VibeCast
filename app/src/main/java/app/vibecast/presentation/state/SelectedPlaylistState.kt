package app.vibecast.presentation.state

import app.vibecast.domain.model.SelectedPlaylistDto

data class SelectedPlaylistState(
    val data : SelectedPlaylistDto? = null,
    val error : String? = null
)