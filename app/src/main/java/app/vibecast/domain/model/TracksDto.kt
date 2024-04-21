package app.vibecast.domain.model

import app.vibecast.data.remote_data.network.music.model.Items


data class TracksDto(
    val href: String,
    val items: Items,
)
