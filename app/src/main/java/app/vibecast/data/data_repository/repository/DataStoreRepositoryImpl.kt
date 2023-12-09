package app.vibecast.data.data_repository.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.vibecast.data.data_repository.repository.Constants.DATASTORE_NAME
import app.vibecast.data.data_repository.repository.Constants.UNIT
import app.vibecast.domain.repository.DataStoreRepository
import app.vibecast.presentation.TAG
import kotlinx.coroutines.flow.first
import javax.inject.Inject


private val Context.dataStore : DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)

class DataStoreRepositoryImpl @Inject constructor(
    private val context: Context
) : DataStoreRepository {

    private val unitKey = stringPreferencesKey(UNIT)

    override suspend fun putUnit(unit: Unit) {
        context.dataStore.edit { preferences ->
            preferences[unitKey] = unit.name
        }
    }

    override suspend fun getUnit(): Unit? {
        return try {
            val preferences = context.dataStore.data.first()
            Log.d(TAG, preferences.toString())
            val unitString = preferences[unitKey]
            unitString?.let { Unit.valueOf(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun clearUnit() {
        context.dataStore.edit { preferences ->
            if (preferences.contains(unitKey)) {
                preferences.remove(unitKey)
            }
        }
    }
}
object Constants {
    const val DATASTORE_NAME = "preferences"
    const val UNIT = "UNIT"
}

enum class Unit {
    IMPERIAL,
    METRIC
}