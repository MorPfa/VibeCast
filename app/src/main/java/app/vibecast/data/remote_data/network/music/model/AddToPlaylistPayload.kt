package app.vibecast.data.remote_data.network.music.model

data class AddToPlaylistPayload(
    val uris : List<String>,
    val position : Int
)
