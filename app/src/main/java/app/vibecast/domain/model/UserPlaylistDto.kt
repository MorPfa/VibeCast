package app.vibecast.domain.model

import app.vibecast.data.remote_data.network.music.model.Item
import com.google.gson.annotations.SerializedName

data class UserPlaylistDto(
    val items: List<Item>,
)