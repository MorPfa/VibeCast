package app.vibecast.presentation.weather

import android.icu.text.SimpleDateFormat
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import app.vibecast.domain.entity.CurrentWeather
import app.vibecast.domain.entity.HourlyWeather
import app.vibecast.domain.entity.ImageDto
import app.vibecast.domain.entity.LocationWithWeatherDataDto
import app.vibecast.domain.entity.WeatherCondition
import app.vibecast.domain.entity.WeatherDto
import app.vibecast.domain.repository.ImageRepository
import app.vibecast.domain.repository.LocationRepository
import app.vibecast.domain.repository.WeatherRepository
import app.vibecast.presentation.ImagePicker
import app.vibecast.presentation.image.ImageLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class CurrentLocationViewModel @Inject constructor(
    private val imageRepository: ImageRepository,
    private val weatherRepository: WeatherRepository,
    private val locationRepository: LocationRepository,
    private val imageLoader: ImageLoader,
    private val imagePicker: ImagePicker
) : ViewModel() {

    private val mutableImage = MutableLiveData<ImageDto>()
    val image : LiveData<ImageDto> get() = mutableImage


    val galleryImages : LiveData<List<ImageDto>> = imageRepository.getLocalImages().asLiveData()
    fun setImageLiveData(image: ImageDto){
        mutableImage.value = image
    }
    fun addImage(imageDto: ImageDto) {
        viewModelScope.launch {
            imageRepository.addImage(imageDto)
        }
    }


    fun deleteImage(imageDto: ImageDto){
        viewModelScope.launch {
            imageRepository.deleteImage(imageDto)
        }
    }

    fun addLocationWeather(location: LocationWithWeatherDataDto) {
        locationRepository.addLocationWeather(location)
    }

    fun loadImage(query: String, weatherCondition: String): Flow<ImageDto?> =
        imagePicker.pickImage(query, weatherCondition).flowOn(Dispatchers.IO)


    fun loadWeather(cityName: String): Flow<WeatherModel> = flow {
        weatherRepository.getWeather(cityName).collect { weatherDto ->
            val convertedWeatherModel = convertWeatherDtoToWeatherModel(weatherDto)
            emit(convertedWeatherModel)
        }
    }

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
            uvi = dto.uvi,
            cloudCover = dto.cloudCover,
            visibility = formatVisibility(dto.visibility) ,
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
            uvi = dto.uvi,
            cloudCover = dto.cloudCover,
            windSpeed = dto.windSpeed,
            weatherConditions = convertWeatherConditions(dto.weatherConditions),
            chanceOfRain = dto.chanceOfRain
        )
    }

    private fun convertWeatherConditions(dtoList: List<WeatherCondition>): List<WeatherModel.WeatherCondition> {
        return dtoList.map { convertWeatherCondition(it) }
    }

    private fun convertWeatherCondition(dto: WeatherCondition): WeatherModel.WeatherCondition {
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

