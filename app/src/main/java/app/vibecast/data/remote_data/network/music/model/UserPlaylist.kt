package app.vibecast.data.remote_data.network.music.model

import com.google.gson.annotations.SerializedName

data class UserPlaylist(
    @SerializedName("description") val description: String,
    @SerializedName("href") val href: String,
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("owner") val owner: Owner,
    @SerializedName("public") val public: Boolean,
    @SerializedName("snapshot_id") val snapshotId: String,
    @SerializedName("tracks") val tracks: UserTracks,
    @SerializedName("uri") val uri: String,
)