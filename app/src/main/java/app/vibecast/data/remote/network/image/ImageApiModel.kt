package app.vibecast.data.remote.network.image

import com.google.gson.annotations.SerializedName

data class ImageApiModel(
   @SerializedName("results") val results : List<Image>
)