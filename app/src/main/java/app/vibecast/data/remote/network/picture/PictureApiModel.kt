package app.vibecast.data.remote.network.picture

import com.squareup.moshi.Json

data class PictureApiModel(
    @Json(name = "results") val results : String
    //TODO figure out real schema for api response
)
