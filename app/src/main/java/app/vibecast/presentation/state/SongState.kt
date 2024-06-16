package app.vibecast.presentation.state

import app.vibecast.data.remote_data.network.music.model.Items

data class SongState(
    val song : Items? = null,
    val error : String? = null
)
