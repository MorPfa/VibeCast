package app.vibecast.data.remote_data.network.music.model

import com.google.gson.annotations.SerializedName

data class ExternalUrls(
    @SerializedName("spotify") val spotify: String,
)