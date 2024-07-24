package app.vibecast.domain.model

import app.vibecast.data.remote_data.network.music.model.Owner
import app.vibecast.data.remote_data.network.music.model.Tracks


data class PlaylistCreatedDto(
    val href: String,
    val id: String,
    val name: String,
    val owner: Owner,
    val public: Boolean,
    val snapshotId: String,
    val tracks: Tracks,
    val uri: String,
)