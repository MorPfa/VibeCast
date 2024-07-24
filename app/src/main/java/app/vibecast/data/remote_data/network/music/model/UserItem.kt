package app.vibecast.data.remote_data.network.music.model

import com.google.gson.annotations.SerializedName

data class UserItem(
    @SerializedName("added_at") val addedAt : String,
    @SerializedName("track") val track : UserTrack,
)
