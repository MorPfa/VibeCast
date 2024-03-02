package app.vibecast.presentation.screens.settings_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.vibecast.domain.repository.music.MusicPreferenceRepository
import app.vibecast.domain.repository.weather.UnitPreferenceRepository
import app.vibecast.domain.repository.weather.Unit
import app.vibecast.domain.repository.music.WeatherCondition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreferencesViewModel
@Inject constructor(
    private val unitPrefRepo: UnitPreferenceRepository,
    private val musicPrefRepo: MusicPreferenceRepository,
) : ViewModel() {

    /**
     * Updates preferred data
     */


    fun savePreferences(unit: Unit) {
        viewModelScope.launch {
            unitPrefRepo.savePreference(unit)

        }
    }

    fun savePreferences(musicPref: Map<WeatherCondition, String>) {
        viewModelScope.launch {
            musicPrefRepo.savePreference(musicPref)

        }
    }

    /**
     * Gets preferred data
     */

    fun getUnitPreferences(): Flow<Unit?> = flow {
        emit(unitPrefRepo.getPreference())
    }

    fun getMusicPreferences(): Flow<Map<WeatherCondition, String>> = flow {
        emit(musicPrefRepo.getPreferences())
    }


    /**
     * Resets preferred data
     */

    fun clearPreferences(prefType: UserPreferences) {
        viewModelScope.launch {
            when (prefType) {
                UserPreferences.WEATHER -> unitPrefRepo.clearPreference()
                UserPreferences.MUSIC -> musicPrefRepo.clearPreference()
            }
        }
    }


}

enum class UserPreferences {
    MUSIC,
    WEATHER
}