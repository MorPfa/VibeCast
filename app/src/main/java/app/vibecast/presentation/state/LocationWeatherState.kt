package app.vibecast.presentation.state

import app.vibecast.presentation.screens.main_screen.weather.LocationWeatherModel

data class LocationWeatherState(
    val combinedData : LocationWeatherModel? = null,
    val error : String? = null
)
