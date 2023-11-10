package app.vibecast.presentation.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.vibecast.domain.usecase.GetCurrentWeatherUseCase
import app.vibecast.presentation.state.UiState

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherUseCase: GetCurrentWeatherUseCase,
    private val weatherConverter: WeatherConverter
) : ViewModel() {


    private val _weatherFlow = MutableStateFlow<UiState<WeatherModel>>(UiState.Loading)

    val weatherFlow: StateFlow<UiState<WeatherModel>> = _weatherFlow

    fun loadWeather(cityName : String) {
        viewModelScope.launch {
            weatherUseCase.execute(GetCurrentWeatherUseCase.Request(cityName))
                .map {
                    weatherConverter.convert(it)
                }
                .collect {
                    _weatherFlow.value = it
                }
        }
    }

}