package app.vibecast.domain.entity



data class WeatherDto (
    val cityName : String,
    val latitude: Double,
    val longitude: Double,
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
