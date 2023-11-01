package app.vibecast.presentation.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.vibecast.domain.usecase.GetWeatherUseCase
import app.vibecast.presentation.state.UiState
import app.vibecast.presentation.weather.WeatherConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherUseCase: GetWeatherUseCase,
    private val weatherConverter: WeatherConverter
) : ViewModel() {


    //TODO fix this

    private val _weatherFlow =
        MutableStateFlow<UiState<WeatherModel>>(UiState.Loading)
    val weatherFlow: StateFlow<UiState<WeatherModel>> = _weatherFlow

    fun loadUser(userId: Long) {
        viewModelScope.launch {
            weatherUseCase.execute(GetWeatherUseCase.Request())
                .map {
                    weatherConverter.convert(it)
                }
                .collect {
                    _weatherFlow.value = it
                }
        }
    }

}