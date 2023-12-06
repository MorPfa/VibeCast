package app.vibecast.data.remote.network.weather

import com.squareup.moshi.Json

data class CityApiModel(
    @Json(name = "name") val cityName: String,
    @Json(name = "country") val countryName: String,
)
