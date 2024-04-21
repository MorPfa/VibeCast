package app.vibecast.data.remote_data.network.music.model

import com.google.gson.annotations.SerializedName

data class Items(
    @SerializedName("name") val name : String,
    @SerializedName("album") val album : Album,
    @SerializedName("artists") val artists : List<Artist>,
    @SerializedName("external_urls") val externalUrls: ExternalUrls,
)
