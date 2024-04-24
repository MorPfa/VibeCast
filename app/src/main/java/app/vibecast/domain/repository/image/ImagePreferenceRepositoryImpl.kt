package app.vibecast.domain.repository.image

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ImagePreferenceRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : ImagePreferenceRepository {

    private val imageKey = stringPreferencesKey(IMAGE)

    override suspend fun saveBackgroundImage(imageUrl: String) {
        dataStore.edit { preferences ->
            preferences[imageKey] = imageUrl
        }
    }


    override fun getBackgroundImage(): Flow<String?> = flow {
        val preferences = dataStore.data.first()
        val imageUrl = preferences[imageKey]
        emit(imageUrl)
    }

    override suspend fun resetBackgroundImage() {
        dataStore.edit { preferences ->
            if (preferences.contains(imageKey)) {
                preferences.remove(imageKey)
            }
        }
    }

    companion object Constants {
        const val IMAGE = "IMAGE"
    }
}