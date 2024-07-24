package app.vibecast.domain.model

import app.vibecast.data.remote_data.network.music.model.Owner
import app.vibecast.data.remote_data.network.music.model.UserTracks


data class SelectedPlaylistDto(
    val description: String,
    val href: String,
    val id: String,
    val name: String,
    val owner: Owner,
    val public: Boolean,
    val snapshotId: String,
    val tracks: UserTracks,
    val uri : String
)
