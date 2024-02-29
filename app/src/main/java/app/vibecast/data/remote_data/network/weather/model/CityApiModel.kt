package app.vibecast.data.remote_data.network.weather.model

import com.squareup.moshi.Json

data class CityApiModel(
    @Json(name = "name") val cityName: String,
    @Json(name = "country") val countryName: String,
)
