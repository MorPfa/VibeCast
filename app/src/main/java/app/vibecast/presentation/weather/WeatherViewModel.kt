package app.vibecast.presentation.weather

import androidx.lifecycle.ViewModel
import app.vibecast.domain.entity.WeatherDto
import app.vibecast.domain.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


@HiltViewModel
class WeatherViewModel @Inject constructor(
   private val weatherRepository: WeatherRepository
) : ViewModel() {


    fun loadWeather(cityName : String) : Flow<WeatherDto> = weatherRepository.getWeather(cityName)


    }

