package app.vibecast.domain.repository

import app.vibecast.domain.repository.implementation.Unit


interface UnitPreferenceRepository {
    suspend fun savePreference(unit: Unit)
    suspend fun getPreference(): Unit
    suspend fun clearPreference()
}