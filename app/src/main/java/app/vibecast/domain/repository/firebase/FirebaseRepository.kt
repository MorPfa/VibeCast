package app.vibecast.domain.repository.firebase

import app.vibecast.domain.model.FirebaseImage
import app.vibecast.domain.model.FirebaseLocation
import app.vibecast.domain.model.FirebaseResponse
import com.google.firebase.auth.FirebaseUser

interface FirebaseRepository {

   suspend fun getImage() : FirebaseResponse<FirebaseImage>

   suspend fun addImage(image : FirebaseImage)

   suspend fun deleteImage(image : FirebaseImage)

   suspend fun getLocation(): FirebaseResponse<FirebaseLocation>
   suspend fun addLocation(location: FirebaseLocation)

   suspend fun deleteLocation(location: FirebaseLocation)
   suspend fun getAllImages() : FirebaseResponse<FirebaseImage>
   suspend fun getAllLocations() : FirebaseResponse<FirebaseLocation>

   suspend fun deleteUserData()

   suspend fun syncData()





}