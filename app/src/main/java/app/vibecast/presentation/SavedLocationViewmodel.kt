package app.vibecast.presentation

import android.icu.text.SimpleDateFormat
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.vibecast.domain.entity.CurrentWeather
import app.vibecast.domain.entity.HourlyWeather
import app.vibecast.domain.entity.ImageDto
import app.vibecast.domain.entity.WeatherCondition
import app.vibecast.domain.entity.WeatherDto
import app.vibecast.domain.repository.WeatherRepository
import app.vibecast.presentation.image.ImageLoader
import app.vibecast.presentation.image.ImagePicker
import app.vibecast.presentation.weather.LocationModel
import app.vibecast.presentation.weather.LocationWeatherModel
import app.vibecast.presentation.weather.WeatherModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SavedLocationViewmodel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val imageLoader: ImageLoader,
    private val imagePicker: ImagePicker
): ViewModel() {

    private val _weather = MutableLiveData<LocationWeatherModel>()
    val weather: LiveData<LocationWeatherModel> get() = _weather

    private val mutableImage = MutableLiveData<ImageDto>()
    val image : LiveData<ImageDto> get() = mutableImage

    fun getSearchedLocationWeather(query: String) {
        viewModelScope.launch {
            val updatedWeatherData = weatherRepository.getWeather(query).collect{
                val weatherData = convertWeatherDtoToWeatherModel(it.weather)
                _weather.value = LocationWeatherModel(
                    location = LocationModel(it.location.cityName, it.location.locationIndex),
                    weather = weatherData
                )
            }

        }
    }
    fun setImageLiveData(image: ImageDto){
        mutableImage.value = image
    }

    fun loadImage(query: String, weatherCondition: String): Flow<ImageDto?> =
        imagePicker.pickImage(query, weatherCondition).flowOn(Dispatchers.IO)

    fun loadImageIntoImageView(url: String, imageView: ImageView) {
        imageLoader.loadUrlIntoImageView(url, imageView)
    }

    private fun convertWeatherDtoToWeatherModel(weatherDto: WeatherDto): WeatherModel {
        return WeatherModel(
            cityName = weatherDto.cityName,
            latitude = weatherDto.latitude,
            longitude = weatherDto.longitude,
            currentWeather = weatherDto.currentWeather?.let { convertCurrentWeather(it) },
            hourlyWeather = weatherDto.hourlyWeather?.map { convertHourlyWeather(it) }
        )
    }

    private fun convertCurrentWeather(dto: CurrentWeather): WeatherModel.CurrentWeather {
        return WeatherModel.CurrentWeather(
            timestamp = convertUnixTimestampToAmPm(dto.timestamp),
            temperature = dto.temperature.toInt(),
            feelsLike = dto.feelsLike.toInt(),
            humidity = dto.humidity,
            uvi = formatUvi(dto.uvi),
            cloudCover = dto.cloudCover,
            visibility = formatVisibility(dto.visibility),
            windSpeed = dto.windSpeed,
            weatherConditions = convertWeatherConditions(dto.weatherConditions)
        )
    }

    private fun convertHourlyWeather(dto: HourlyWeather): WeatherModel.HourlyWeather {
        return WeatherModel.HourlyWeather(
            timestamp = convertUnixTimestampToAmPm(dto.timestamp),
            temperature = dto.temperature.toInt(),
            feelsLike = dto.feelsLike,
            humidity = dto.humidity,
            uvi = formatUvi(dto.uvi),
            windSpeed = dto.windSpeed,
            weatherConditions = convertWeatherConditions(dto.weatherConditions),
            chanceOfRain = formatProp(dto.chanceOfRain)
        )
    }

    private fun convertWeatherConditions(dtoList: List<WeatherCondition>): List<WeatherModel.WeatherCondition> {
        return dtoList.map { convertWeatherCondition(it) }
    }

    private fun convertWeatherCondition(dto: WeatherCondition): WeatherModel.WeatherCondition {
        return WeatherModel.WeatherCondition(
            mainDescription = dto.mainDescription,
            icon = dto.icon
        )
    }

    private fun convertUnixTimestampToAmPm(unixTimestamp: Long): String {
        val date = Date(unixTimestamp * 1000L)
        val sdf = SimpleDateFormat("ha", Locale.getDefault())
        return sdf.format(date)
    }

    private fun formatProp(prop : Double) : Int = (prop * 100).toInt()


    private fun formatUvi(uvi : Double) : Double = uvi * 10


    private fun formatVisibility(visibility: Int): String {
        return when {
            visibility >= 1000 -> {
                val miles = visibility / 1000.0 * 0.621371
                String.format("%.1f miles", miles)
            }
            else -> {
                "$visibility meters"
            }
        }
    }

}