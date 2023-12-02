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
import app.vibecast.domain.entity.LocationDto
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
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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

    val weather: LiveData<LocationWeatherModel> = weatherRepository.getWeather("Chicago")
        .map { locationWithWeatherDataDto ->
            // Here, complete the mapping of locationWithWeatherDataDto to LocationWeatherModel
            val weatherData = convertWeatherDtoToWeatherModel(locationWithWeatherDataDto.weather)
            val locationData = LocationModel(
                locationWithWeatherDataDto.location.cityName,
                locationWithWeatherDataDto.location.locationIndex
            )
            LocationWeatherModel(location = locationData, weather = weatherData)
        }
        .asLiveData()





    private val mutableLocationList = MutableLiveData<LocationDto>()
    val locations : LiveData<LocationDto> get() = mutableLocationList
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

    fun loadImage(query: String, weatherCondition: String): Flow<ImageDto?> =
        imagePicker.pickImage(query, weatherCondition).flowOn(Dispatchers.IO)


    fun loadWeather(cityName: String): Flow<LocationWeatherModel> = weatherRepository.getWeather(cityName).map { data ->
            val weatherData = convertWeatherDtoToWeatherModel(data.weather)
            val locationData = LocationModel(data.location.cityName, data.location.locationIndex)
        LocationWeatherModel(location = locationData, weather = weatherData)
    }

    fun loadImageIntoImageView(url: String, imageView: ImageView) {
        imageLoader.loadUrlIntoImageView(url, imageView)
    }


    // Extension functions for WeatherCondition
    private fun WeatherCondition.toWeatherConditionModel(): WeatherModel.WeatherCondition {
        return WeatherModel.WeatherCondition(
            conditionId = conditionId,
            mainDescription = mainDescription,
            detailedDescription = detailedDescription,
            icon = icon
        )
    }

    // Extension function for List<WeatherCondition>
    private fun List<WeatherCondition>.toWeatherConditionModelList(): List<WeatherModel.WeatherCondition> {
        return map { it.toWeatherConditionModel() }
    }
    fun CurrentWeather.toCurrentWeatherModel(): WeatherModel.CurrentWeather {
        return WeatherModel.CurrentWeather(
            timestamp = convertUnixTimestampToAmPm(timestamp),
            temperature = temperature.toInt(),
            feelsLike = feelsLike.toInt(),
            humidity = humidity,
            uvi = formatUvi(uvi),
            cloudCover = cloudCover,
            visibility = formatVisibility(visibility),
            windSpeed = windSpeed,
            weatherConditions = weatherConditions.toWeatherConditionModelList()
        )
    }

    // Extension functions for HourlyWeather
    fun HourlyWeather.toHourlyWeatherModel(): WeatherModel.HourlyWeather {
        return WeatherModel.HourlyWeather(
            timestamp = convertUnixTimestampToAmPm(timestamp),
            temperature = temperature.toInt(),
            feelsLike = feelsLike,
            humidity = humidity,
            uvi = formatUvi(uvi),
            cloudCover = cloudCover,
            windSpeed = windSpeed,
            weatherConditions = weatherConditions.toWeatherConditionModelList(),
            chanceOfRain = formatProp(chanceOfRain)
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
            uvi = formatUvi(dto.uvi),
            cloudCover = dto.cloudCover,
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
            conditionId = dto.conditionId,
            mainDescription = dto.mainDescription,
            detailedDescription = dto.detailedDescription,
            icon = dto.icon
        )
    }
}

