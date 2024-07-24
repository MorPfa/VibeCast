package app.vibecast.data.remote_data.network.music.model

data class CreatePlaylistPayload (
    val name : String,
    val public : Boolean? = null,
    val description : String? = null,
)