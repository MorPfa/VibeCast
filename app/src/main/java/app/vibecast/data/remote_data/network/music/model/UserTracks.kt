package app.vibecast.data.remote_data.network.music.model

import com.google.gson.annotations.SerializedName

data class UserTracks(
    @SerializedName("items") val items : List<UserItem>

)