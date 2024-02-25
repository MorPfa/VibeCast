package app.vibecast.data.remote.network.music

import com.google.gson.annotations.SerializedName

data class Tracks(
    @SerializedName("href") val href: String,
    @SerializedName("total") val total: Int,
)