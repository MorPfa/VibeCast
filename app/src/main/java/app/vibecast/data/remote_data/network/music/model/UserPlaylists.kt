package app.vibecast.data.remote_data.network.music.model

import com.google.gson.annotations.SerializedName

data class UserPlaylists(
    @SerializedName("href") val href : String,
    @SerializedName("limit") val limit : Int,
    @SerializedName("next") val next : String,
    @SerializedName("previous") val previous : String,
    @SerializedName("total") val total : Int,
    @SerializedName("items") val items : List<Item>
)