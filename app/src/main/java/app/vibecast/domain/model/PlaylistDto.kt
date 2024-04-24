package app.vibecast.domain.model

import app.vibecast.data.remote_data.network.music.model.Item

data class PlaylistDto(
    val href: String,
    val items: List<Item>,
    val limit: Int,
    val next: String,
    val offset: Int,
    val total: Int,
)