package app.vibecast.domain.repository.implementation//package app.vibecast.data.data_repository.repository
//
//import android.content.Context
//import androidx.datastore.core.DataStore
//import androidx.datastore.preferences.core.Preferences
//import androidx.datastore.preferences.core.edit
//import androidx.datastore.preferences.core.stringPreferencesKey
//import androidx.datastore.preferences.preferencesDataStore
//import app.vibecast.data.data_repository.repository.Constants.DATASTORE_NAME
//import app.vibecast.data.data_repository.repository.Constants.UNIT
//import app.vibecast.domain.repository.DataStoreRepository
//import kotlinx.coroutines.flow.first
//import javax.inject.Inject
//
//
//private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)
//
//class DataStoreRepositoryImpl @Inject constructor(
//    private val context: Context
//) : DataStoreRepository {
//
//    private val unitKey = stringPreferencesKey(UNIT)
//
//    /**
//     *  Updates preferred Unit for Weather
//     */
//    override suspend fun savePreferences(unit: Unit) {
//        context.dataStore.edit { preferences ->
//            preferences[unitKey] = unit.name
//        }
//    }
//    /**
//     *  Gets preferred Unit for Weather
//     */
//    override suspend fun getPreferences(): Unit? {
//        return try {
//            val preferences = context.dataStore.data.first()
//            val unitString = preferences[unitKey]
//            unitString?.let { Unit.valueOf(it) }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
//    /**
//     *  Resets preferred Unit for Weather
//     */
//    override suspend fun clearPreferences() {
//        context.dataStore.edit { preferences ->
//            if (preferences.contains(unitKey)) {
//                preferences.remove(unitKey)
//            }
//        }
//    }
//}
//
//
//
