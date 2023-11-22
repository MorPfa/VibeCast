package app.vibecast.presentation.weather

import android.icu.text.SimpleDateFormat
import android.widget.ImageView
import androidx.lifecycle.ViewModel
import app.vibecast.domain.entity.ImageDto
import app.vibecast.domain.repository.WeatherRepository
import app.vibecast.presentation.ImagePicker
import app.vibecast.presentation.image.ImageLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.Date
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class CurrentLocationViewModel @Inject constructor(
   private val weatherRepository: WeatherRepository,
    private val imageLoader: ImageLoader,
    private val imagePicker: ImagePicker
) : ViewModel() {

    fun loadImage(query: String, weatherCondition: String): Flow<ImageDto?> = flow {
        imagePicker.pickImage(query, weatherCondition).collect { imageDto ->
            emit(imageDto)
        }
    }.flowOn(Dispatchers.IO)


    fun loadWeather(cityName: String): Flow<WeatherModel> = flow {
        weatherRepository.getWeather(cityName).collect { weatherDto ->
            val convertedWeatherModel = convertWeatherDtoToWeatherModel(weatherDto)
            emit(convertedWeatherModel)
        }
    }

    fun loadImageIntoImageView(url: String, imageView: ImageView) {
        imageLoader.loadUrlIntoImageView(url, imageView)
    }

    private fun convertWeatherDtoToWeatherModel(weatherDto: app.vibecast.domain.entity.WeatherDto): WeatherModel {
        return WeatherModel(
            cityName = weatherDto.cityName,
            latitude = weatherDto.latitude,
            longitude = weatherDto.longitude,
            currentWeather = weatherDto.currentWeather?.let { convertCurrentWeather(it) },
            hourlyWeather = weatherDto.hourlyWeather?.map { convertHourlyWeather(it) }
        )
    }

    private fun convertCurrentWeather(dto: app.vibecast.domain.entity.CurrentWeather): WeatherModel.CurrentWeather {
        return WeatherModel.CurrentWeather(
            timestamp = convertUnixTimestampToAmPm(dto.timestamp),
            temperature = dto.temperature.toInt(),
            feelsLike = dto.feelsLike.toInt(),
            humidity = dto.humidity,
            uvi = dto.uvi,
            cloudCover = dto.cloudCover,
            visibility = formatVisibility(dto.visibility) ,
            windSpeed = dto.windSpeed,
            weatherConditions = convertWeatherConditions(dto.weatherConditions)
        )
    }

    private fun convertHourlyWeather(dto: app.vibecast.domain.entity.HourlyWeather): WeatherModel.HourlyWeather {
        return WeatherModel.HourlyWeather(
            timestamp = convertUnixTimestampToAmPm(dto.timestamp),
            temperature = dto.temperature.toInt(),
            feelsLike = dto.feelsLike,
            humidity = dto.humidity,
            uvi = dto.uvi,
            cloudCover = dto.cloudCover,
            windSpeed = dto.windSpeed,
            weatherConditions = convertWeatherConditions(dto.weatherConditions),
            chanceOfRain = dto.chanceOfRain
        )
    }

    private fun convertWeatherConditions(dtoList: List<app.vibecast.domain.entity.WeatherCondition>): List<WeatherModel.WeatherCondition> {
        return dtoList.map { convertWeatherCondition(it) }
    }

    private fun convertWeatherCondition(dto: app.vibecast.domain.entity.WeatherCondition): WeatherModel.WeatherCondition {
        return WeatherModel.WeatherCondition(
            conditionId = dto.conditionId,
            mainDescription = dto.mainDescription,
            detailedDescription = dto.detailedDescription,
            icon = dto.icon
        )
    }

    private fun convertUnixTimestampToAmPm(unixTimestamp: Long): String {
        val date = Date(unixTimestamp * 1000L)
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(date)
    }


    private fun formatVisibility(visibility: Int): String {
        return when {
            visibility >= 1000 -> {
                val kilometers = visibility / 1000.0
                String.format("%.1f km", kilometers)
            }
            else -> {
                "$visibility meters"
            }
        }
    }
}

