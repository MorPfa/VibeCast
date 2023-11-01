package app.vibecast.presentation.weather

import androidx.lifecycle.ViewModel
import app.vibecast.domain.usecase.GetWeatherUseCase
import app.vibecast.presentation.weather.WeatherConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherUseCase: GetWeatherUseCase,
    private val weatherConverter: WeatherConverter
) : ViewModel() {




}