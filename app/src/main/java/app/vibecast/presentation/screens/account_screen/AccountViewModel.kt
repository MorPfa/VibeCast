package app.vibecast.presentation.screens.account_screen


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.vibecast.domain.model.LocationDto
import app.vibecast.domain.repository.firebase.FirebaseRepository
import app.vibecast.domain.repository.weather.LocationRepository
import app.vibecast.presentation.screens.main_screen.weather.LocationModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private var auth: FirebaseAuth = Firebase.auth

    private val _userName = MutableLiveData<String?>()

    init {
        _userName.value = auth.currentUser?.displayName
    }

    val userName: LiveData<String?>
        get() = _userName


    fun updateUserName(userName : String){
        _userName.value = userName
    }


    fun addLocation(location: LocationModel) {
        locationRepository.addLocation(LocationDto(location.cityName, location.country))

    }

    fun deleteLocation(location: LocationDto) {
        locationRepository.deleteLocation(LocationDto(location.cityName, location.country))
    }



}