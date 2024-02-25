package app.vibecast.data.remote.network.music

import com.google.gson.annotations.SerializedName

data class ExternalUrls(
    @SerializedName("spotify") val spotify: String,
)