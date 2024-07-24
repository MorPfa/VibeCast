package app.vibecast.data.remote_data.network.music.model

data class RemoveFromPlaylistPayload(
    val tracks : List<Uri>,
    val snapshotId : String? = null
)
data class Uri(
    val uri : String
)