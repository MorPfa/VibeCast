package app.vibecast.data.remote.network.weather

import com.squareup.moshi.Json

data class CoordinateApiModel(
    @Json(name = "lat") val latitude : Double,
    @Json(name = "lon") val longitude : Double
)