package app.vibecast.domain.repository.music

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementation of [MusicPreferenceRepository]
 *
 * Methods:
 * - [savePreference] Save map of music genres user linked with weather conditions
 * - [getPreferences] Gets all set preferences for music genres
 * - [getPreference] Gets preferred music genre for specified weather condition
 * - [clearPreference] Resets music preferences
 */
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
                it ?: when(weather){
                    WeatherCondition.CLOUDY -> "Ambient"
                    WeatherCondition.FOGGY -> "Jazz"
                    WeatherCondition.RAINY -> "Lo-fi"
                    WeatherCondition.STORMY -> "Heavy-Metal"
                    WeatherCondition.SNOWY -> "Classical"
                    WeatherCondition.SUNNY -> "Pop"
                }
            }
            genre
        }catch (e : Exception){
            when(weather){
                WeatherCondition.CLOUDY -> "Ambient"
                WeatherCondition.FOGGY -> "Jazz"
                WeatherCondition.RAINY -> "Lo-fi"
                WeatherCondition.STORMY -> "Heavy-Metal"
                WeatherCondition.SNOWY -> "Classical"
                WeatherCondition.SUNNY -> "Pop"
            }
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

                    Timber.tag("musicPref").d("Unknown key: $key")
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
