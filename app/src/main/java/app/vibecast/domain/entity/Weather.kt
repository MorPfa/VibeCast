package app.vibecast.domain.entity



data class Weather (
    val cityName : String,
    val latitude: Double?,
    val longitude: Double?,
    val currentWeather: CurrentWeather?,
    val hourlyWeather: List<HourlyWeather>?
)


data class CurrentWeather(
    val timestamp: Long,
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
    val timestamp: Long,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val uvi: Double,
    val cloudCover: Int,
    val windSpeed: Double,
    val weatherConditions: List<WeatherCondition>,
    val chanceOfRain: Double
)

data class WeatherCondition(
    val conditionId: Int,
    val mainDescription: String,
    val detailedDescription: String,
    val icon: String
)
