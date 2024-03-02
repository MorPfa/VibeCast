package app.vibecast.domain.repository.music


interface MusicPreferenceRepository {

    suspend fun savePreference(preference: Map<WeatherCondition, String>)
    suspend fun getPreferences(): Map<WeatherCondition, String>

    suspend fun getPreference(weather : WeatherCondition) : String
    suspend fun clearPreference()
}