package app.vibecast.domain.repository.image

import kotlinx.coroutines.flow.Flow

interface ImagePreferenceRepository {

    suspend fun saveBackgroundImage(imageUrl: String)
    fun getBackgroundImage(): Flow<String?>
    suspend fun resetBackgroundImage()
}