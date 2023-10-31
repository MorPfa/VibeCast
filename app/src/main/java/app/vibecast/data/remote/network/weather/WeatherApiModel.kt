package app.vibecast.data.remote.network.weather

import com.squareup.moshi.Json

data class WeatherApiModel(
    val cityName : String,
    @Json(name = "lat") val latitude: Double,
    @Json(name = "lon") val longitude: Double,
    @Json(name = "current") val currentWeatherRemote: CurrentWeatherRemote,
    @Json(name = "hourly") val hourlyWeather: List<HourlyWeatherRemote>
)

data class CurrentWeatherRemote(
    @Json(name = "dt") val timestamp: Long,
    @Json(name = "temp") val temperature: Double,
    @Json(name = "feels_like") val feelsLike: Double,
    @Json(name = "humidity") val humidity: Int,
    @Json(name = "uvi") val uvi: Double,
    @Json(name = "clouds") val cloudCover: Int,
    @Json(name = "visibility") val visibility: Int,
    @Json(name = "wind_speed") val windSpeed: Double,
    @Json(name = "weather") val weatherConditionRemotes: List<WeatherConditionRemote>
)

data class HourlyWeatherRemote(
    @Json(name = "dt") val timestamp: Long,
    @Json(name = "temp") val temperature: Double,
    @Json(name = "feels_like") val feelsLike: Double,
    @Json(name = "humidity") val humidity: Int,
    @Json(name = "uvi") val uvi: Double,
    @Json(name = "clouds") val cloudCover: Int,
    @Json(name = "wind_speed") val windSpeed: Double,
    @Json(name = "weather") val weatherConditionRemotes: List<WeatherConditionRemote>,
    @Json(name = "pop") val chanceOfRain: Double
)

data class WeatherConditionRemote(
    @Json(name = "id") val conditionId: Int,
    @Json(name = "main") val mainDescription: String,
    @Json(name = "description") val detailedDescription: String,
    @Json(name = "icon") val icon: String
)
