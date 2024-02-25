package app.vibecast.data.remote.network.music

import com.google.gson.annotations.SerializedName

data class Item(
    @SerializedName("description") val description: String,
    @SerializedName("external_urls") val externalUrls: ExternalUrls,
    @SerializedName("href") val href: String,
    @SerializedName("id") val id: String,
    @SerializedName("images") val images: List<Image>,
    @SerializedName("name") val name: String,
    @SerializedName("owner") val owner: Owner,
    @SerializedName("primary_color") val primaryColor: String,
    @SerializedName("public") val public: Boolean,
    @SerializedName("snapshot_id") val snapshotId: String,
    @SerializedName("tracks") val tracks: Tracks,
    @SerializedName("type") val type: String,
    @SerializedName("uri") val uri: String,
)