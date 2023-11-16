package app.vibecast.domain.entity

data class LocationWithWeatherDataDto(
    val location: LocationDto,
    var weather: WeatherDto
)

