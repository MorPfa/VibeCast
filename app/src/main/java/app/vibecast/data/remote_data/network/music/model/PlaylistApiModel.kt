package app.vibecast.data.remote_data.network.music.model

import com.google.gson.annotations.SerializedName

data class PlaylistApiModel(
    @SerializedName("playlists") val playlists: Playlists,
)