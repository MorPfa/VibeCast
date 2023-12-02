package app.vibecast.presentation.navigation


import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import app.vibecast.domain.entity.CurrentWeather
import app.vibecast.domain.entity.HourlyWeather
import app.vibecast.domain.entity.LocationDto
import app.vibecast.domain.entity.LocationWithWeatherDataDto
import app.vibecast.domain.entity.WeatherCondition
import app.vibecast.domain.entity.WeatherDto
import app.vibecast.domain.repository.LocationRepository
import app.vibecast.domain.repository.UserRepository
import app.vibecast.presentation.weather.LocationModel
import app.vibecast.presentation.weather.LocationWeatherModel
import app.vibecast.presentation.weather.WeatherModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    fun addLocation(location: LocationModel) {
        locationRepository.addLocation(LocationDto(location.cityName, location.locationIndex))
    }

    fun deleteLocation(location: LocationDto) {
        locationRepository.deleteLocation(LocationDto(location.cityName, location.locationIndex))
    }

    var savedLocations : LiveData<List<LocationDto>> = locationRepository.getLocations().asLiveData()


    private fun WeatherModel.WeatherCondition.toWeatherConditionDto(): WeatherCondition {
        return WeatherCondition(
            mainDescription = mainDescription,
            icon = icon
        )
    }

    private fun List<WeatherModel.WeatherCondition>.toWeatherConditionDtoList(): List<WeatherCondition> {
        return map { it.toWeatherConditionDto() }
    }

    private fun WeatherModel.CurrentWeather.toCurrentWeatherDto(): CurrentWeather {
        return CurrentWeather(
            timestamp = 0,  // Assuming timestamp is already in the correct format
            temperature = temperature.toDouble(),
            feelsLike = feelsLike.toDouble(),
            humidity = humidity,
            uvi = uvi,
            cloudCover = cloudCover,
            visibility = visibility.toInt(),  // Assuming visibility is already in the correct format
            windSpeed = windSpeed,
            weatherConditions = weatherConditions.toWeatherConditionDtoList()
        )
    }

    private fun WeatherModel.HourlyWeather.toHourlyWeatherDto(): HourlyWeather {
        return HourlyWeather(
            timestamp = 0,
            temperature = temperature.toDouble(),
            feelsLike = feelsLike,
            humidity = humidity,
            uvi = uvi,
            windSpeed = windSpeed,
            weatherConditions = weatherConditions.toWeatherConditionDtoList(),
            chanceOfRain = chanceOfRain.toDouble()
        )
    }

    private fun WeatherModel.toWeatherDto(): WeatherDto {
        return WeatherDto(
            cityName = cityName,
            latitude = latitude,
            longitude = longitude,
            currentWeather = currentWeather?.toCurrentWeatherDto(),
            hourlyWeather = hourlyWeather?.map { it.toHourlyWeatherDto() }
        )
    }

    private fun LocationWeatherModel.toLocationWithWeatherDataDto(): LocationWithWeatherDataDto {
        return LocationWithWeatherDataDto(
            location = LocationDto(cityName = location.cityName, locationIndex = location.locationIndex),
            weather = weather.toWeatherDto()
        )
    }

}