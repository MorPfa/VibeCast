package app.vibecast.data.remote_data.network.music.model

import com.google.gson.annotations.SerializedName

data class TracksResponse(
    @SerializedName("tracks") val tracks: Tracks,
) {
    data class Tracks(
        @SerializedName("href") val href: String,
        @SerializedName("items") val items: List<Items>,
    )
}