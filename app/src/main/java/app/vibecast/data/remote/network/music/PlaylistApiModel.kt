package app.vibecast.data.remote.network.music

import com.google.gson.annotations.SerializedName

data class PlaylistApiModel(
    @SerializedName("playlists") val playlists: Playlists,
)