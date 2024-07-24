package app.vibecast.data.remote_data.network.music.model

import com.google.gson.annotations.SerializedName

data class UserTrack(
    @SerializedName("album") val album: Album,
    @SerializedName("artists") val artists: List<Artist>,
    @SerializedName("external_urls") val externalUrls: ExternalUrls,
    @SerializedName("href") val href: String,
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("uri") val uri: String,
)
