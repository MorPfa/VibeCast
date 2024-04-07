package app.vibecast.domain.repository.firebase

import app.vibecast.data.local_data.data_source.image.LocalImageDataSource
import app.vibecast.data.local_data.data_source.weather.LocalLocationDataSource
import app.vibecast.domain.model.FirebaseImage
import app.vibecast.domain.model.FirebaseLocation
import app.vibecast.domain.model.FirebaseResponse
import app.vibecast.domain.util.Constants.USERS_REF
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject


class FirebaseRepositoryImpl @Inject constructor(
    private val locationDataSource: LocalLocationDataSource,
    private val imageDataSource: LocalImageDataSource,
) : FirebaseRepository {


    private val databaseRoot: DatabaseReference = Firebase.database.reference
    private val usersRef: DatabaseReference = databaseRoot.child(USERS_REF)
    private val auth: FirebaseAuth = Firebase.auth
    private val currentUser = auth.currentUser


    override suspend fun syncData() {
        syncLocationData()
        syncImageData()
    }

    private suspend fun syncLocationData() {
        locationDataSource.getLocations().collect { localData ->
            val localDataCount = localData.size
            val firebaseData = getAllLocations().data
            val firebaseCount = firebaseData?.size
            if (localDataCount != firebaseCount) {
                localData.forEach { location ->
                    val containsLocation = firebaseData?.any { firebaseLocation ->
                        firebaseLocation.city == location.cityName
                    } ?: false
                    if (!containsLocation) {
                        addLocation(
                            FirebaseLocation(
                                id = location.cityName,
                                city = location.cityName,
                                country = location.country
                            )
                        )
                    }
                }
            }

        }
    }

    private suspend fun syncImageData() {
        imageDataSource.getImages().collect { localImages ->
            val localDataCount = localImages.size
            val firebaseData = getAllImages().data
            val firebaseCount = firebaseData?.size
            if (localDataCount != firebaseCount) {
                localImages.forEach { image ->
                    val containsImage = firebaseData?.any { firebaseImage ->
                        firebaseImage.id == image.id
                    } ?: false
                    if (!containsImage) {
                        addImage(
                            FirebaseImage(
                                id = image.id,
                                url = image.urls.regular
                            )
                        )
                    }
                }
            }

        }
    }

    override suspend fun deleteUserData() {
        currentUser?.let { user ->
            val userRef = usersRef.child(user.uid)
            userRef.removeValue().addOnCompleteListener { databaseTask ->
                if (databaseTask.isSuccessful) {
                    // User data successfully deleted from the database
                    Timber.tag("firebaseDB").d("Deleted user data successfully")

                } else {
                    // Failed to delete user data from the database
                    Timber.tag("firebaseDB").d("Failed user data deletion successfully")
                }
            }
        }
    }


    override suspend fun deleteImage(image: FirebaseImage) {
        currentUser?.let { user ->
            val userImageRef = usersRef.child(user.uid).child("images")
            val imageItemRef = userImageRef.child(image.id)
            imageItemRef.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.tag("firebaseDB").d("Image deleted successfully")
                } else {
                    Timber.tag("firebaseDB").d("Failed to delete image: ${task.exception}")
                }
            }
        }
    }

    override suspend fun deleteLocation(location: FirebaseLocation) {
        currentUser?.let { user ->
            val userLocationsRef = usersRef.child(user.uid).child("locations")
            val locationItemRef = userLocationsRef.child(location.id)
            locationItemRef.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.tag("firebaseDB").d("Location deleted successfully")
                } else {
                    Timber.tag("firebaseDB").d("Failed to delete location: ${task.exception}")
                }
            }
        }
    }


    override suspend fun addImage(image: FirebaseImage) {
        currentUser?.let { user ->
            val userImagesRef = usersRef.child(user.uid).child("images")
            userImagesRef.child(image.id).setValue(image)
                .addOnSuccessListener {
                    Timber.tag("firebaseDB").d("Added image successfully")
                }
                .addOnFailureListener { exception ->
                    Timber.tag("firebaseDB").d("Couldnt add image $exception")
                }
        }
    }

    override suspend fun addLocation(location: FirebaseLocation) {
        currentUser?.let { user ->
            val userLocationsRef = usersRef.child(user.uid).child("locations")


            userLocationsRef.child(location.id).setValue(location)
                .addOnSuccessListener {
                    Timber.tag("firebaseDB").d("Added location successfully")
                }
                .addOnFailureListener { exception ->
                    Timber.tag("firebaseDB").d("Couldn't add location $exception")
                }
        }
    }

    override suspend fun getImage(): FirebaseResponse<FirebaseImage> {
        val response = FirebaseResponse<FirebaseImage>()
        try {
            response.data =
                currentUser?.let { user ->
                    usersRef.child(user.uid).child("images").get()
                        .await().children.map { snapShot ->
                            snapShot.getValue(FirebaseImage::class.java)!!
                        }
                }
        } catch (exception: Exception) {
            response.exception = exception
        }
        return response
    }


    override suspend fun getLocation(): FirebaseResponse<FirebaseLocation> {
        val response = FirebaseResponse<FirebaseLocation>()
        try {
            response.data =
                currentUser?.let { user ->
                    usersRef.child(user.uid).child("locations").get()
                        .await().children.map { snapShot ->
                            snapShot.getValue(FirebaseLocation::class.java)!!
                        }
                }
        } catch (exception: Exception) {
            response.exception = exception
        }
        return response
    }

    override suspend fun getAllImages(): FirebaseResponse<FirebaseImage> {
        val response = FirebaseResponse<FirebaseImage>()
        try {
            currentUser?.let { user ->
                val imagesRef = usersRef.child(user.uid).child("images")
                imagesRef.get().addOnSuccessListener { dataSnapshot ->
                    val imagesList = dataSnapshot.children.mapNotNull { imageSnapshot ->
                        imageSnapshot.getValue(FirebaseImage::class.java)
                    }
                    response.data = imagesList
                    Timber.tag("firebaseDB").d("Retrieved images successfully")
                }.addOnFailureListener { exception ->
                    response.exception = exception
                    Timber.tag("firebaseDB").d("Error getting images: $exception")
                }
            }
        } catch (exception: Exception) {
            response.exception = exception
        }
        return response
    }


    override suspend fun getAllLocations(): FirebaseResponse<FirebaseLocation> {
        val response = FirebaseResponse<FirebaseLocation>()
        try {
            currentUser?.let { user ->
                val locationsRef = usersRef.child(user.uid).child("locations")
                locationsRef.get().addOnSuccessListener { dataSnapshot ->
                    val locationList = dataSnapshot.children.mapNotNull { locationSnapshot ->
                        locationSnapshot.getValue(FirebaseLocation::class.java)
                    }
                    response.data = locationList
                    Timber.tag("firebaseDB").d("Retrieved locations successfully")
                }.addOnFailureListener { exception ->
                    response.exception = exception
                    Timber.tag("firebaseDB").d("Error getting locations: $exception")
                }
            }
        } catch (exception: Exception) {
            response.exception = exception
        }
        return response
    }
}



