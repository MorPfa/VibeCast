package app.vibecast.data.remote_data.network.music.model

import com.google.gson.annotations.SerializedName

data class Album(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("external_urls") val externalUrls: ExternalUrls,

)
