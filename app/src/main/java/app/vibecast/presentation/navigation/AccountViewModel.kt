package app.vibecast.presentation.navigation


import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import app.vibecast.domain.entity.LocationDto
import app.vibecast.domain.entity.WeatherCondition
import app.vibecast.domain.repository.LocationRepository
import app.vibecast.domain.repository.UserRepository
import app.vibecast.presentation.weather.LocationModel
import app.vibecast.presentation.weather.WeatherModel
import dagger.hilt.android.lifecycle.HiltViewModel
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



}