package app.vibecast.presentation.mainscreen

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import app.vibecast.data.TAGS.IMAGE_ERROR
import app.vibecast.data.data_repository.repository.Unit
import app.vibecast.data.remote.LocationGetter
import app.vibecast.domain.entity.CurrentWeather
import app.vibecast.domain.entity.HourlyWeather
import app.vibecast.domain.entity.ImageDto
import app.vibecast.domain.entity.LocationDto
import app.vibecast.domain.entity.WeatherCondition
import app.vibecast.domain.entity.WeatherDto
import app.vibecast.domain.repository.DataStoreRepository
import app.vibecast.domain.repository.ImageRepository
import app.vibecast.domain.repository.LocationRepository
import app.vibecast.domain.repository.WeatherRepository
import app.vibecast.presentation.image.ImageLoader
import app.vibecast.presentation.image.ImagePicker
import app.vibecast.presentation.permissions.LocationPermissionState
import app.vibecast.presentation.weather.LocationModel
import app.vibecast.presentation.weather.LocationWeatherModel
import app.vibecast.presentation.weather.WeatherModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val locationGetter: LocationGetter,
    private val weatherRepository: WeatherRepository,
    private val locationRepository: LocationRepository,
    private val imageRepository: ImageRepository,
    private val imageLoader: ImageLoader,
    private val imagePicker: ImagePicker,
    private val dataStoreRepository: DataStoreRepository
): ViewModel() {


    private val _image = MutableLiveData<ImageDto>()
    val image: LiveData<ImageDto> get() = _image

    fun loadImageIntoImageView(url: String, imageView: ImageView) {
        imageLoader.loadUrlIntoImageView(url, imageView)
    }

    fun loadImage(query: String, weatherCondition: String): Flow<ImageDto?> = flow {
        emitAll(imagePicker.pickImage(query, weatherCondition).flowOn(Dispatchers.IO))
    }.catch { e ->
        Log.e(IMAGE_ERROR, "Error loading image: $e")
        throw e
    }

    fun setImageLiveData(image: ImageDto) {
        _image.value = image
    }

    fun addImage(imageDto: ImageDto) {
        viewModelScope.launch {
            try {
                imageRepository.addImage(imageDto)
            } catch (e: Exception) {
                Log.e(IMAGE_ERROR,e.toString())
                throw e
            }
        }
    }

    fun deleteImage(imageDto: ImageDto) {
        viewModelScope.launch {
            try {
                imageRepository.deleteImage(imageDto)
            } catch (e: Exception) {
                Log.e(IMAGE_ERROR,e.toString())
                throw e
            }
        }
    }


    val galleryImages: LiveData<List<ImageDto>> = imageRepository.getLocalImages().asLiveData()

    private val _currentWeather = MutableLiveData<LocationWeatherModel>()
    val currentWeather: LiveData<LocationWeatherModel> get() = _currentWeather

    private val _savedWeather = MutableLiveData<LocationWeatherModel>()
    val savedWeather: LiveData<LocationWeatherModel> get() = _savedWeather


    fun getSearchedLocationWeather(query: String) {
        viewModelScope.launch {
            weatherRepository.getWeather(query).collect {
                val weatherData = convertWeatherDtoToWeatherModel(it.weather)
                _currentWeather.value = LocationWeatherModel(
                    location = LocationModel(it.location.cityName, it.location.country),
                    weather = weatherData
                )
            }

        }
    }

    private var _locations = MutableLiveData<List<LocationDto>>()
    val locations: LiveData<List<LocationDto>> get() = _locations

    private var _locationIndex = MutableLiveData(0)

    val locationIndex: LiveData<Int> get() = _locationIndex

    fun incrementIndex() {
        _locationIndex.value = _locationIndex.value?.plus(1)
    }

    fun decrementIndex() {
        _locationIndex.value = _locationIndex.value?.minus(1)
    }

    fun getSavedLocationWeather() {
        viewModelScope.launch {
            locationRepository.getLocations().collect {
                _locations.value = it
                if (locationIndex.value != null && it.isNotEmpty()) {
                    weatherRepository.getWeather(it[_locationIndex.value!!].cityName)
                        .collect { data ->
                            val weatherData = convertWeatherDtoToWeatherModel(data.weather)
                            _savedWeather.value = LocationWeatherModel(
                                location = LocationModel(
                                    data.location.cityName,
                                    data.location.country
                                ),
                                weather = weatherData
                            )
                        }
                }
            }

        }
    }

    private val _locationPermissionState =
        MutableStateFlow<LocationPermissionState>(LocationPermissionState.Granted)
    private val locationPermissionState: StateFlow<LocationPermissionState> =
        _locationPermissionState

    fun updatePermissionState(state: LocationPermissionState) {
        _locationPermissionState.value = state
    }

    @SuppressLint("MissingPermission")
    fun loadCurrentLocationWeather() {
        viewModelScope.launch {
            when (locationPermissionState.value) {
                LocationPermissionState.Granted -> {
//                    Log.d(TAG, "permission granted")
                    locationGetter.client.lastLocation
                        .addOnSuccessListener { location ->
//                            Log.d(TAG, location.latitude.toString())
//                            Log.d(TAG, location.longitude.toString())
                            // Check if the location is not null before proceeding
                            if (location != null) {
//                                Log.d(TAG, "location not null")
                                viewModelScope.launch {
                                    weatherRepository.getWeather(
                                        location.latitude,
                                        location.longitude
                                    )
                                        .map { locationWithWeatherDataDto ->
                                            val weatherData =
                                                convertWeatherDtoToWeatherModel(
                                                    locationWithWeatherDataDto.weather
                                                )
                                            val locationData = LocationModel(
                                                locationWithWeatherDataDto.location.cityName,
                                                locationWithWeatherDataDto.location.country
                                            )
                                            LocationWeatherModel(
                                                location = locationData,
                                                weather = weatherData
                                            )
                                        }
                                        .collect {
                                            _currentWeather.value = it

                                        }
                                }
                            } else {
                                // location is null show seattle
//                                Log.d(TAG, "location is null")
                                viewModelScope.launch {
                                    weatherRepository.getWeather("Chicago")
                                        .map { locationWithWeatherDataDto ->
                                            val weatherData =
                                                convertWeatherDtoToWeatherModel(
                                                    locationWithWeatherDataDto.weather
                                                )
                                            val locationData = LocationModel(
                                                locationWithWeatherDataDto.location.cityName,
                                                locationWithWeatherDataDto.location.country
                                            )
                                            LocationWeatherModel(
                                                location = locationData,
                                                weather = weatherData
                                            )
                                        }
                                        .collect {
                                            _currentWeather.value = it
//
                                        }
                                }
                            }
                        }
                }

                else -> {
                    //location permission is denied
                    viewModelScope.launch {
                        weatherRepository.getWeather("Seattle")
                            .map { locationWithWeatherDataDto ->
                                val weatherData =
                                    convertWeatherDtoToWeatherModel(locationWithWeatherDataDto.weather)
                                val locationData = LocationModel(
                                    locationWithWeatherDataDto.location.cityName,
                                    locationWithWeatherDataDto.location.country
                                )
                                LocationWeatherModel(location = locationData, weather = weatherData)
                            }
                    }
                }
            }
        }
    }


    private fun convertUnixTimestampToAmPm(unixTimestamp: Long): String {
        val date = Date(unixTimestamp * 1000L)
        val sdf = SimpleDateFormat("ha", Locale.getDefault())
        //TODO fix time format
        return sdf.format(date)
    }

    private fun formatProp(prop: Double): Int = (prop * 100).toInt()


    private fun formatUvi(uvi: Double): Double = uvi * 10


    private suspend fun formatVisibility(visibility: Int): String = suspendCoroutine { continuation ->
        var formattedVisibility: String

        viewModelScope.launch {
            when (dataStoreRepository.getUnit()) {
                Unit.IMPERIAL -> formattedVisibility = when {
                    visibility >= 1000 -> {
                        val miles = visibility / 1000.0
                        String.format("%.1f miles", miles)
                    }

                    else -> {
                        "$visibility feet"
                    }
                }

                Unit.METRIC -> formattedVisibility = when {
                    visibility >= 1000 -> {
                        val km = visibility / 1000.0
                        String.format("%.1f km", km)
                    }

                    else -> {
                        "$visibility meters"
                    }
                }

                else -> {
                    formattedVisibility = when {
                        visibility >= 1000 -> {
                            val miles = visibility / 1000.0
                            String.format("%.1f miles", miles)
                        }

                        else -> {
                            "$visibility meters"
                        }
                    }
                }
            }

            continuation.resume(formattedVisibility)
        }
    }



    private fun formatWindSpeed(ws : Double) : String {
        var formattedWs = String.format("%.1f mph", ws)
        viewModelScope.launch {
            formattedWs = when (dataStoreRepository.getUnit()) {
                Unit.IMPERIAL -> String.format("%.1f mph", ws)
                Unit.METRIC -> String.format("%.1f kmh", ws)
                        else -> {
                            String.format("%.1f mph", ws)
                        }
                    }

        }
    return formattedWs
    }







    private suspend fun convertWeatherDtoToWeatherModel(weatherDto: WeatherDto): WeatherModel {
        return WeatherModel(
            cityName = weatherDto.cityName,
            latitude = weatherDto.latitude,
            longitude = weatherDto.longitude,
            currentWeather = weatherDto.currentWeather?.let { convertCurrentWeather(it) },
            hourlyWeather = weatherDto.hourlyWeather?.map { convertHourlyWeather(it) }
        )
    }

    private suspend fun convertCurrentWeather(dto: CurrentWeather): WeatherModel.CurrentWeather {
        return WeatherModel.CurrentWeather(
            timestamp = convertUnixTimestampToAmPm(dto.timestamp),
            temperature = dto.temperature.toInt(),
            feelsLike = dto.feelsLike.toInt(),
            humidity = dto.humidity,
            uvi = formatUvi(dto.uvi),
            cloudCover = dto.cloudCover,
            visibility = formatVisibility(dto.visibility),
            windSpeed = formatWindSpeed(dto.windSpeed),
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
