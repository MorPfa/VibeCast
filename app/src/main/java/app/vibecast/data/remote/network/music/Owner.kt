package app.vibecast.data.remote.network.music

import com.google.gson.annotations.SerializedName

data class Owner(
    @SerializedName("display_name") val displayName: String,
    @SerializedName("external_urls") val externalUrls: ExternalUrls,
    @SerializedName("href") val href: String,
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("uri") val uri: String,
)