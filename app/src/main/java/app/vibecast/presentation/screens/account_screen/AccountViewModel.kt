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
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val locationRepository: LocationRepository,
) : ViewModel() {

    private var auth: FirebaseAuth = Firebase.auth

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String?>
        get() = _userName

    private val _userEmail = MutableLiveData<String?>()
    val userEmail: LiveData<String?>
        get() = _userEmail


    fun updateUserName(name: String) {
        _userName.value = name

    }



    suspend fun addUserName(user: FirebaseUser?, userName: String): Boolean {
        val deferred = CompletableDeferred<Boolean>()

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(userName)
            .build()

        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { profileUpdateTask ->
                if (profileUpdateTask.isSuccessful) {
                    Timber.tag("auth").d("User profile updated with username")
                    _userName.value = userName
                    deferred.complete(true)
                } else {
                    Timber.tag("auth").e(
                        profileUpdateTask.exception,
                        "Failed to update user profile with username"
                    )
                    deferred.complete(false)
                }
            }

        return deferred.await()
    }

    init {
        _userName.value = auth.currentUser?.displayName
        _userEmail.value = auth.currentUser?.email
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

    fun syncData() {
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


    fun addLocation(location: LocationModel) {
        locationRepository.addLocation(LocationDto(location.cityName, location.country))

    }

    fun deleteLocation(location: LocationDto) {
        locationRepository.deleteLocation(LocationDto(location.city, location.country))
    }


}