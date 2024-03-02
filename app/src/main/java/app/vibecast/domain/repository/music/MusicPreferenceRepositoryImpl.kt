package app.vibecast.domain.repository.music

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import app.vibecast.presentation.TAG
import kotlinx.coroutines.flow.first
import javax.inject.Inject


class MusicPreferenceRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : MusicPreferenceRepository {



    override suspend fun savePreference(preference: Map<WeatherCondition, String>) {
        dataStore.edit { preferences ->
            preference.forEach { (weatherCondition, musicGenre) ->
                preferences[stringPreferencesKey(weatherCondition.name)] = musicGenre
            }
        }
    }


    override suspend fun getPreference(weather: WeatherCondition): String {
        return try{
            val preferences = dataStore.data.first()
            val genre = preferences[stringPreferencesKey(weather.name)].let {
                it ?: "Jazz"
            }
            genre
        }catch (e : Exception){
            "Jazz"
        }
    }

    override suspend fun getPreferences(): Map<WeatherCondition, String> {
        return try {
            val preferencesMap = dataStore.data.first().asMap()
            val resultMap = mutableMapOf<WeatherCondition, String>()
            preferencesMap.forEach { (key, value) ->
                val weatherCondition = WeatherCondition.entries.find { it.name == key.name }
                if (weatherCondition != null) {
                    resultMap[weatherCondition] = value.toString()
                } else {

                    Log.d(TAG, "Unknown key: $key")
                }
            }
            resultMap
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }






    override suspend fun clearPreference() {
      dataStore.edit { preferences ->
            preferences.clear()
        }
    }


}
enum class WeatherCondition {
    SUNNY, RAINY, CLOUDY, SNOWY, FOGGY, STORMY
}
