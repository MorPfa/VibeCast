package app.vibecast.data.remote_data.network.music.model

import com.google.gson.annotations.SerializedName

data class Artist(
    @SerializedName("external_urls") val externalUrls: ExternalUrls
)
