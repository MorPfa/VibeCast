package app.vibecast.presentation.mainscreen

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import app.vibecast.data.TAGS.IMAGE_ERROR
import app.vibecast.data.TAGS.WEATHER_ERROR
import app.vibecast.data.data_repository.repository.Unit
import app.vibecast.data.remote.LocationGetter
import app.vibecast.domain.entity.CurrentWeather
import app.vibecast.domain.entity.HourlyWeather
import app.vibecast.domain.entity.ImageDto
import app.vibecast.domain.entity.LocationDto
import app.vibecast.domain.entity.LocationWithWeatherDataDto
import app.vibecast.domain.entity.WeatherCondition
import app.vibecast.domain.entity.WeatherDto
import app.vibecast.domain.repository.DataStoreRepository
import app.vibecast.domain.repository.ImageRepository
import app.vibecast.domain.repository.LocationRepository
import app.vibecast.domain.repository.WeatherRepository
import app.vibecast.presentation.TAG
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    init {
        viewModelScope.launch {
            locationRepository.getLocations().collect { locations ->
                withContext(Dispatchers.Main){   _locations.value = locations}

            }
        }
    }


    private val _image = MutableLiveData<ImageDto>()
    val image: LiveData<ImageDto> get() = _image

    private val _imageCount = MutableLiveData(0)

    val imageCount = _imageCount

    fun setImageCountLiveData(){
        viewModelScope.launch {
            imageRepository.getLocalImages().collect{
                withContext(Dispatchers.Main){
                    _imageCount.value = it.size
                }
            }
        }

    }

    fun pickDefaultImage(weatherCondition: String) : Int = imagePicker.pickDefaultImage(weatherCondition)

    fun loadImageIntoImageView(url: String, imageView: ImageView) {
        imageLoader.loadUrlIntoImageView(url, imageView)
    }

    fun loadImage(query: String, weatherCondition: String): Flow<ImageDto?> = flow {
        emitAll(imagePicker.pickImage(query, weatherCondition).flowOn(Dispatchers.IO))
    }.catch { e ->
        Log.e(IMAGE_ERROR, "Error loading image: $e in ViewModel")
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

    fun getImageForDownload(query: String) : Flow<String> = flow {
        imageRepository.getImageForDownload(query).collect{
            emit(it)
        }

    }


    val galleryImages: LiveData<List<ImageDto>> = imageRepository.getLocalImages().asLiveData()

    private val _currentWeather = MutableLiveData<LocationWeatherModel>()
    val currentWeather: LiveData<LocationWeatherModel> get() = _currentWeather

    private val _savedWeather = MutableLiveData<LocationWeatherModel>()
    val savedWeather: LiveData<LocationWeatherModel> get() = _savedWeather


    fun getSearchedLocationWeather(query: String) {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                weatherRepository.getSearchedWeather(query).collect { data ->
                    try {
                        val weatherData = convertWeatherDtoToWeatherModel(data.weather)
                        _currentWeather.value = LocationWeatherModel(
                            location = LocationModel(data.location.cityName, data.location.country),
                            weather = weatherData
                        )
                    } catch (e: Exception) {
                        Log.d(WEATHER_ERROR, "$query viewmodel")
                        throw e
                    }
                }
            } catch (e: Exception) {
                Log.d(WEATHER_ERROR, "$query viewmodel")
                throw e
            }
        }
    }



    private var _locations = MutableLiveData<List<LocationDto>>()
    val locations: LiveData<List<LocationDto>> get() = _locations


    fun addLocation(location: LocationModel) {
        locationRepository.addLocation(LocationDto(location.cityName, location.country))
    }

    fun deleteLocation(location: LocationDto) {
        locationRepository.deleteLocation(LocationDto(location.cityName, location.country))
    }

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
            try {
                locationRepository.getLocations().distinctUntilChanged().collect { locations ->
                    if (locationIndex.value != null && locations.isNotEmpty()) {
                        try {
                            weatherRepository.getWeather(locations[locationIndex.value!!].cityName).distinctUntilChanged()
                                .collect { data ->
                                    try {
                                        val weatherData = convertWeatherDtoToWeatherModel(data.weather)
                                        _savedWeather.value = LocationWeatherModel(
                                            location = LocationModel(
                                                data.location.cityName,
                                                data.location.country
                                            ),
                                            weather = weatherData
                                        )
                                    } catch (e: Exception) {
                                        handleError(e)
                                    }
                                }
                        } catch (e: Exception) {
                            handleError(e)
                        }
                    }
                }
            } catch (e: Exception) {
                handleError(e)
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


    fun loadCurrentLocationWeather() {
        viewModelScope.launch(Dispatchers.IO)  {
            when (locationPermissionState.value) {
                LocationPermissionState.Granted -> handleLocationGranted()
                else -> handleLocationDenied()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun handleLocationGranted() {
        locationGetter.client.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            fetchWeatherData(location.latitude, location.longitude)
                        } catch (e: Exception) {
                            // Handle specific error for fetching weather data
                            handleError(e)
                        }
                    }
                } else {
                    // location is null, show default location
                    viewModelScope.launch(Dispatchers.IO)  {
                        try {
                            fetchDefaultWeatherData()
                        } catch (e: Exception) {
                            handleError(e)
                        }
                    }
                }
            }
    }

    private suspend fun handleLocationDenied() {
        try {
            fetchDefaultWeatherData()
        } catch (e: Exception) {
            handleError(e)
        }
    }

    private suspend fun fetchWeatherData(lat: Double, lon: Double) {
        try {
            weatherRepository.getWeather(lat, lon).collect { weatherData ->
                updateWeatherData(weatherData)
            }
        } catch (e: Exception) {
            handleError(e)
        }
    }

    private suspend fun fetchDefaultWeatherData() {
        val defaultLocation = getDefaultLocation()
        try {
            weatherRepository.getWeather(defaultLocation).collect { weatherData ->
                updateWeatherData(weatherData)
            }
        } catch (e: Exception) {
            handleError(e)
        }
    }

    private suspend fun updateWeatherData(locationWithWeatherDataDto: LocationWithWeatherDataDto) {
        try {
            val weatherData = convertWeatherDtoToWeatherModel(locationWithWeatherDataDto.weather)
            val locationData = LocationModel(
                locationWithWeatherDataDto.location.cityName,
                locationWithWeatherDataDto.location.country
            )
            withContext(Dispatchers.Main){
                _currentWeather.value = LocationWeatherModel(location = locationData, weather = weatherData)
            }
        } catch (e: Exception) {
            handleError(e)
        }
    }

    private fun getDefaultLocation(): String {
        return "Chicago"
    }

    private fun handleError(e: Exception) {
        Log.e(TAG, "Error: $e")
        throw e
    }




    private fun convertUnixTimestamp(unixTimestamp: Long, timeZoneId: String): String {
        val date = Date(unixTimestamp * 1000L)
        val sdf = SimpleDateFormat("ha", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone(timeZoneId)
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
            timezone = weatherDto.timezone,
            currentWeather = weatherDto.currentWeather?.let { convertCurrentWeather(it, weatherDto.timezone) },
            hourlyWeather = weatherDto.hourlyWeather?.map { convertHourlyWeather(it, weatherDto.timezone) }
        )
    }

    private suspend fun convertCurrentWeather(dto: CurrentWeather, timeZoneId: String): WeatherModel.CurrentWeather {
        return WeatherModel.CurrentWeather(
            timestamp = convertUnixTimestamp(dto.timestamp,timeZoneId),
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

    private fun convertHourlyWeather(dto: HourlyWeather, timeZoneId: String): WeatherModel.HourlyWeather {
        return WeatherModel.HourlyWeather(
            timestamp = convertUnixTimestamp(dto.timestamp, timeZoneId),
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
