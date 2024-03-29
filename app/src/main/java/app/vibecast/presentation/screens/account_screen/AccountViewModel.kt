package app.vibecast.presentation.screens.account_screen


import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import app.vibecast.domain.model.LocationDto
import app.vibecast.domain.model.WeatherCondition
import app.vibecast.domain.repository.weather.LocationRepository
import app.vibecast.domain.repository.user.UserRepository
import app.vibecast.presentation.screens.main_screen.weather.LocationModel
import app.vibecast.presentation.screens.main_screen.weather.WeatherModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    fun addLocation(location: LocationModel) {
        locationRepository.addLocation(LocationDto(location.cityName, location.country))
    }

    fun deleteLocation(location: LocationDto) {
        locationRepository.deleteLocation(LocationDto(location.cityName, location.country))
    }

    var savedLocations : LiveData<List<LocationModel>> = locationRepository.getLocations().asLiveData()


    private fun WeatherModel.WeatherCondition.toWeatherConditionDto(): WeatherCondition {
        return WeatherCondition(
            mainDescription = mainDescription,
            icon = icon
        )
    }



}