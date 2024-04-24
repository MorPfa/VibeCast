package app.vibecast.domain.repository.weather

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Implementation of [UnitPreferenceRepository]
 *
 * Methods:
 * - [savePreference] Sets preferred Unit for weather data
 * - [getPreference] Gets preferred Unit for weather data
 * - [clearPreference] Resets preferred Unit for Weather
 */
class WeatherUnitRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : UnitPreferenceRepository {


    private val unitKey = stringPreferencesKey(UNIT)
    override suspend fun savePreference(unit: Unit) {
        dataStore.edit { preferences ->
            preferences[unitKey] = unit.name
        }
    }
    override suspend fun getPreference(): Unit {
        return try {
            val preferences = dataStore.data.first()
            val unitString = preferences[unitKey]
            unitString.let {
                if (it != null) {
                    Unit.valueOf(it)
                } else {
                    Unit.IMPERIAL
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Unit.IMPERIAL
        }
    }

    override suspend fun clearPreference() {
       dataStore.edit { preferences ->
            if (preferences.contains(unitKey)) {
                preferences.remove(unitKey)
            }
        }
    }

    companion object Constants {
        const val UNIT = "UNIT"
    }

}

enum class Unit {
    IMPERIAL, METRIC
}