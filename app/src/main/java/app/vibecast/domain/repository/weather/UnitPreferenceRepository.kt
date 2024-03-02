package app.vibecast.domain.repository.weather


interface UnitPreferenceRepository {
    suspend fun savePreference(unit: Unit)
    suspend fun getPreference(): Unit
    suspend fun clearPreference()
}