package app.vibecast.presentation.weather

import android.content.Context
import app.vibecast.R
import app.vibecast.domain.usecase.GetCurrentWeatherUseCase
import app.vibecast.presentation.state.CommonResultConverter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import javax.inject.Inject

class WeatherConverter @Inject constructor(@ApplicationContext private val context: Context) :
    CommonResultConverter<GetCurrentWeatherUseCase.Response, WeatherModel>() {

    private fun formatTimeStamp(timeStamp : Long) : String {
        val sdf = SimpleDateFormat("hh:mm a")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = Date(timeStamp * 1000)
        return sdf.format(date)


    }
    override fun convertSuccess(data: GetCurrentWeatherUseCase.Response): WeatherModel {
        val cityName = context.getString(R.string.center_location_text, data.weather.cityName)
        val currentTimestamp = context.getString(R.string.left_time, data.weather.currentWeather?.let {
            formatTimeStamp(it.timestamp)
            }
        )
        val currentTemp = context.getString(R.string.center_temperature_text, data.weather.currentWeather?.temperature)

        context.getString(R.string.left_temp, data.weather.currentWeather?.temperature)

        val currentFeelsLike = context.getString(R.string.feels_like_value, data.weather.currentWeather?.feelsLike)
        val currentHumidity = context.getString(R.string.humidity, data.weather.currentWeather?.humidity)

        val currentUvi = context.getString(R.string.uv_index_value, data.weather.currentWeather?.uvi)

        val currentChanceOfRain = context.getString(R.string.chance_of_rain_value,
            data.weather.hourlyWeather?.get(0)?.chanceOfRain
        )


        val currentVisibility = context.getString(R.string.visibility_value, data.weather.currentWeather?.visibility)

        val currentWindspeed = context.getString(R.string.wind_speed_value, data.weather.currentWeather?.windSpeed)

        val currentWeatherCondition = context.getString(R.string.left_weather_condition,
            data.weather.hourlyWeather?.get(0)?.weatherConditions?.get(0)?.mainDescription
        )

        val firstHourlyTimestamp = context.getString(R.string.center_time, data.weather.currentWeather?.let {
            formatTimeStamp(it.timestamp)
        }
        )
        val firstHourlyTemp = context.getString(R.string.center_temp, data.weather.currentWeather?.temperature)


        val firstHourlyWeatherCondition = context.getString(R.string.center_weather_condition,
            data.weather.hourlyWeather?.get(0)?.weatherConditions?.get(0)?.mainDescription
        )


        val secondHourlyTimestamp = context.getString(R.string.right_time, data.weather.hourlyWeather?.get(1)?.let {
            formatTimeStamp(it.timestamp)
        }
        )
        val secondHourlyTemp = context.getString(R.string.right_temp, data.weather.hourlyWeather?.get(1)?.temperature)


        val secondHourlyWeatherCondition = context.getString(R.string.right_weather_condition,
            data.weather.hourlyWeather?.get(1)?.weatherConditions?.get(0)?.mainDescription
        )
        val currentWeather = data.weather.currentWeather?.let { it ->
            WeatherModel.CurrentWeatherModel(
                currentTimestamp,
                currentTemp,
                currentFeelsLike,
                currentHumidity,
                currentUvi,
                currentVisibility,
                currentWindspeed,
                it.weatherConditions.map { WeatherModel.WeatherConditionModel(it.mainDescription) }
            )
        }

        val hourlyWeather = data.weather.hourlyWeather?.take(3)?.map {
            WeatherModel.HourlyWeatherModel(
                firstHourlyTimestamp,
                firstHourlyTemp,
                firstHourlyWeatherCondition,
                secondHourlyTimestamp,
                secondHourlyTemp,
                secondHourlyWeatherCondition
            )

        }

        return WeatherModel(
            cityName,
            currentWeather!!,
            hourlyWeather!!
        )
    }
}
