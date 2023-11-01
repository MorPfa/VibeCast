package app.vibecast.presentation.weather

import android.content.Context
import app.vibecast.domain.usecase.GetWeatherUseCase
import app.vibecast.presentation.state.CommonResultConverter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WeatherConverter @Inject constructor(@ApplicationContext private val context: Context) :
    CommonResultConverter<GetWeatherUseCase.Response, WeatherModel>() {
    override fun convertSuccess(data: GetWeatherUseCase.Response): WeatherModel {
        return WeatherModel()
    }
}