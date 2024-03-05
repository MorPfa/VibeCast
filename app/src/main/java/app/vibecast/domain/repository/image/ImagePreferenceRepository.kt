package app.vibecast.domain.repository.image

import app.vibecast.domain.repository.music.WeatherCondition
import kotlinx.coroutines.flow.Flow

interface ImagePreferenceRepository {

    suspend fun savePreference(imageUrl: String)
    fun getPreference(): Flow<String?>
    suspend fun clearPreference()
}