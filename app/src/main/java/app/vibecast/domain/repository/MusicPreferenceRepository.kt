package app.vibecast.domain.repository

import app.vibecast.domain.repository.implementation.WeatherCondition


interface MusicPreferenceRepository {

    suspend fun savePreference(preference: Map<WeatherCondition, String>)
    suspend fun getPreference(): Map<WeatherCondition, String>
    suspend fun clearPreference()
}