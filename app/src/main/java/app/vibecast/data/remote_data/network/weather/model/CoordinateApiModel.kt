package app.vibecast.data.remote_data.network.weather.model

import com.squareup.moshi.Json

data class CoordinateApiModel(
    @Json(name = "name") val name : String,
    @Json(name = "lat") val latitude : Double,
    @Json(name = "lon") val longitude : Double,
    @Json(name = "country") val country : String,

)
