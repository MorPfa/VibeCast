package app.vibecast.presentation.weather



data class WeatherModel(
    val cityName: String,
    val currentWeather: CurrentWeatherModel,
    val hourlyWeather: List<HourlyWeatherModel>
) {
    data class CurrentWeatherModel(
        val timestamp: String,
        val temperature: String,
        val feelsLike: String,
        val humidity: String,
        val uvi: String,
        val visibility: String,
        val windSpeed: String,
        val weatherConditions: List<WeatherConditionModel>
    )

    data class HourlyWeatherModel(
        val firstHourTimeStamp : String,
        val firstHourTemp : String,
        val firstHourWeatherCondition : String,
        val secondHourTimestamp: String,
        val secondHourTemperature: String,
        val secondHourWeatherCondition: String
    )

    data class WeatherConditionModel(
        val mainDescription: String
    )
}
