package app.vibecast.data.remote_data.network.music.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: String,
    @SerializedName("display_name") val name: String,
    @SerializedName("uri") val uri: String,
)