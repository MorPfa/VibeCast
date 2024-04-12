package app.vibecast.domain.repository.firebase

import app.vibecast.domain.model.FirebaseImage
import app.vibecast.domain.model.FirebaseLocation
import app.vibecast.domain.model.FirebaseMusicPreferences
import app.vibecast.domain.model.FirebaseResponse
import app.vibecast.domain.util.DB_STATUS
import kotlinx.coroutines.flow.Flow

interface FirebaseRepository {

    suspend fun getImage(): FirebaseResponse<FirebaseImage>

    suspend fun addImage(image: FirebaseImage)

    suspend fun deleteImage(image: FirebaseImage)

    suspend fun getLocation(): FirebaseResponse<FirebaseLocation>
    suspend fun addLocation(location: FirebaseLocation)

    suspend fun deleteLocation(location: FirebaseLocation)
    fun getAllImages(): Flow<FirebaseResponse<FirebaseImage>>
    fun getAllLocations(): Flow<FirebaseResponse<FirebaseLocation>>

    suspend fun deleteUserData()
    suspend fun syncData()

    fun updateImageCount(action: Action)
    fun updateLocationCount(action: Action)




}

enum class Action {
    DELETE, INSERT
}