package app.vibecast.presentation.screens.settings_screen

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.vibecast.domain.repository.music.MusicPreferenceRepository
import app.vibecast.domain.repository.weather.UnitPreferenceRepository
import app.vibecast.domain.repository.weather.Unit
import app.vibecast.domain.repository.music.WeatherCondition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    fun updateMusicPreferences() {
        viewModelScope.launch(Dispatchers.IO) {
           val preferences =  musicPrefRepo.getPreferences()
            withContext(Dispatchers.Main){
                musicPreferences.value = preferences
            }

        }
    }


   val musicPreferences = MutableLiveData<Map<WeatherCondition, String>>()



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