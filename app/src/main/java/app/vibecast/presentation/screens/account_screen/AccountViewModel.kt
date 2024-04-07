package app.vibecast.presentation.screens.account_screen


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.vibecast.domain.model.FirebaseImage
import app.vibecast.domain.model.FirebaseLocation
import app.vibecast.domain.model.ImageDto
import app.vibecast.domain.model.LocationDto
import app.vibecast.domain.repository.firebase.FirebaseRepository
import app.vibecast.domain.repository.weather.LocationRepository
import app.vibecast.presentation.screens.main_screen.weather.LocationModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val locationRepository: LocationRepository,
) : ViewModel() {

    private var auth: FirebaseAuth = Firebase.auth

    private val _userName = MutableLiveData<String?>()

    init {
        _userName.value = auth.currentUser?.displayName
    }


    fun addImageToFirebase(image: ImageDto) {
        viewModelScope.launch(Dispatchers.IO) {
            firebaseRepository.addImage(FirebaseImage(image.id, image.urls.regular))
        }

    }

    fun addLocationToFirebase(location: LocationModel) {
        Timber.tag("firebaseDB").d("called")
        viewModelScope.launch(Dispatchers.IO) {
            firebaseRepository.addLocation(
                FirebaseLocation(
                    country = location.country,
                    city = location.cityName,
                    id = location.cityName,

                    )
            )
        }
    }

    fun deleteUserData() {
        viewModelScope.launch(Dispatchers.IO) {
            firebaseRepository.deleteUserData()
        }
    }

    fun syncData(){
        viewModelScope.launch(Dispatchers.IO) {
            firebaseRepository.syncData()
        }
    }

    fun deleteLocationFromFirebase(location: LocationModel) {
        Timber.tag("firebaseDB").d("called")
        viewModelScope.launch(Dispatchers.IO) {
            firebaseRepository.deleteLocation(
                FirebaseLocation(
                    country = location.country,
                    city = location.cityName,
                    id = location.cityName,
                    )
            )
        }
    }

    fun deleteImageFromFirebase(image: ImageDto) {
        Timber.tag("firebaseDB").d("called")
        viewModelScope.launch(Dispatchers.IO) {
            firebaseRepository.deleteImage(
                FirebaseImage(
                    url = image.urls.regular,
                    id = image.id,
                )
            )
        }
    }



    fun getImageFromFirebase() {

    }

    fun getLocationFromFirebase() {

    }

    val userName: LiveData<String?>
        get() = _userName


    fun updateUserName(userName: String) {
        _userName.value = userName
    }


    fun addLocation(location: LocationModel) {
        locationRepository.addLocation(LocationDto(location.cityName, location.country))

    }

    fun deleteLocation(location: LocationDto) {
        locationRepository.deleteLocation(LocationDto(location.cityName, location.country))
    }


}