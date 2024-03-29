package app.vibecast.presentation.screens.main_screen.weather




data class WeatherModel(
    val cityName : String,
    val latitude: Double,
    val longitude: Double,
    val timezone : String,
    val currentWeather: CurrentWeather?,
    val hourlyWeather: List<HourlyWeather>?
) {

    data class CurrentWeather(
        var timestamp: String,
        val temperature: Int,
        val feelsLike: Int,
        val humidity: Int,
        val uvi: Double,
        val cloudCover: Int,
        val visibility: String,
        val windSpeed: String,
        val weatherConditions: List<WeatherCondition>
    )

    data class HourlyWeather(
        var timestamp: String,
        val temperature: Int,
        val feelsLike: Double,
        val humidity: Int,
        val uvi: Double,
        val windSpeed: Double,
        val weatherConditions: List<WeatherCondition>,
        val chanceOfRain: Int
    )

    data class WeatherCondition(
        val mainDescription: String,
        val icon: String
    )
}