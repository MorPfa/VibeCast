package app.vibecast.data.remote_data.network.image.model

import com.google.gson.annotations.SerializedName

data class DownloadUrl(
    @SerializedName("url") val url :String,
)