package app.vibecast.domain.model

data class LocationWithWeatherDataDto(
    val location: LocationDto,
    var weather: WeatherDto
)

