package app.vibecast.data.remote.network.image

import com.google.gson.annotations.SerializedName

data class DownloadUrl(
    @SerializedName("url") val url :String,
)