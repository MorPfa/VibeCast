package app.vibecast.data.remote_data.network.music.model


import com.google.gson.annotations.SerializedName

data class Playlists(
    @SerializedName("href") val href: String,
    @SerializedName("items") val items: List<Item>,
    @SerializedName("limit") val limit: Int,
    @SerializedName("next") val next: String,
    @SerializedName("offset") val offset: Int,
    @SerializedName("total") val total: Int,
)