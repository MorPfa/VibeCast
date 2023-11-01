package app.vibecast.presentation.weather

import android.content.Context
import app.vibecast.R
import app.vibecast.domain.usecase.GetWeatherUseCase
import app.vibecast.presentation.state.CommonResultConverter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class WeatherConverter @Inject constructor(@ApplicationContext private val context: Context) :
    CommonResultConverter<GetWeatherUseCase.Response, WeatherModel>() {
    override fun convertSuccess(data: GetWeatherUseCase.Response): WeatherModel {
        return WeatherModel(
            context.getString(R.string.center_location_text, data.weather.cityName),
            data.weather.latitude,
            data.weather.longitude,
            data.weather.currentWeather,
            data.weather.hourlyWeather

//TODO figure out how to change data arrangement to make it work like in 8.02
        )
    }
}