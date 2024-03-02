package app.vibecast.domain.model

import app.vibecast.domain.repository.weather.Unit


data class WeatherDto(
    var cityName : String,
    var country : String,
    val latitude: Double,
    val longitude: Double,
    val dataTimestamp : Long,
    val timezone : String,
    val unit : Unit?,
    val currentWeather: CurrentWeather?,
    val hourlyWeather: List<HourlyWeather>?
)


data class CurrentWeather(
    var timestamp: Long,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val uvi: Double,
    val cloudCover: Int,
    val visibility: Int,
    val windSpeed: Double,
    val weatherConditions: List<WeatherCondition>
)

data class HourlyWeather(
    var timestamp: Long,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val uvi: Double,
    val windSpeed: Double,
    val weatherConditions: List<WeatherCondition>,
    val chanceOfRain: Double
)

data class WeatherCondition(
    val mainDescription: String,
    val icon: String
)
