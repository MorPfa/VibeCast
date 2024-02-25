package app.vibecast.data.remote.network.music

import com.google.gson.annotations.SerializedName

data class Image(
    @SerializedName("height") val height: Any,
    @SerializedName("width") val width: Any,
    @SerializedName("url") val url: String,
)