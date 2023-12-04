package app.vibecast.presentation

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import app.vibecast.data.remote.LocationGetter
import app.vibecast.domain.entity.CurrentWeather
import app.vibecast.domain.entity.HourlyWeather
import app.vibecast.domain.entity.ImageDto
import app.vibecast.domain.entity.WeatherCondition
import app.vibecast.domain.entity.WeatherDto
import app.vibecast.domain.repository.ImageRepository
import app.vibecast.domain.repository.WeatherRepository
import app.vibecast.presentation.image.ImageLoader
import app.vibecast.presentation.image.ImagePicker
import app.vibecast.presentation.weather.LocationModel
import app.vibecast.presentation.weather.LocationWeatherModel
import app.vibecast.presentation.weather.WeatherModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val locationGetter: LocationGetter,
    private val weatherRepository: WeatherRepository,
    private val imageRepository: ImageRepository,
    private val imageLoader: ImageLoader,
    private val imagePicker: ImagePicker,
): ViewModel() {

    private val _image = MutableLiveData<ImageDto>()
    val image : LiveData<ImageDto> get() = _image

    val galleryImages : LiveData<List<ImageDto>> = imageRepository.getLocalImages().asLiveData()

    private val _weather = MutableLiveData<LocationWeatherModel>()
    val weather : LiveData<LocationWeatherModel> get() = _weather



    private val _locationPermissionState = MutableStateFlow<LocationPermissionState>(LocationPermissionState.Granted)
    val locationPermissionState: StateFlow<LocationPermissionState> = _locationPermissionState
    @SuppressLint("MissingPermission")
    fun loadCurrentLocationWeather() {
        viewModelScope.launch {
            if (locationGetter.isPermissionGranted()) {
                _locationPermissionState.value = LocationPermissionState.Granted
                weatherRepository.getWeather(
                    locationGetter.client.lastLocation.result.latitude,
                    locationGetter.client.lastLocation.result.longitude
                ).map { locationWithWeatherDataDto ->
                    val weatherData =
                        convertWeatherDtoToWeatherModel(locationWithWeatherDataDto.weather)
                    val locationData = LocationModel(
                        locationWithWeatherDataDto.location.cityName,
                        locationWithWeatherDataDto.location.locationIndex
                    )
                    LocationWeatherModel(location = locationData, weather = weatherData)
                }.collect {
                    _weather.value = it
                }
            }
            else {
                _locationPermissionState.value = LocationPermissionState.RequestPermission
                if (_locationPermissionState.value == LocationPermissionState.Granted) {

                }
            }
        }
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

    }
