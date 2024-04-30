package app.vibecast.presentation.screens.main_screen

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import app.vibecast.domain.model.CurrentWeather
import app.vibecast.domain.model.HourlyWeather
import app.vibecast.domain.model.LocationDto
import app.vibecast.domain.model.LocationWithWeatherDataDto
import app.vibecast.domain.model.WeatherCondition
import app.vibecast.domain.model.WeatherDto
import app.vibecast.domain.repository.weather.LocationRepository
import app.vibecast.domain.repository.weather.Unit
import app.vibecast.domain.repository.weather.UnitPreferenceRepository
import app.vibecast.domain.repository.weather.WeatherRepository
import app.vibecast.domain.util.LocationGetter
import app.vibecast.domain.util.TAGS.WEATHER_ERROR
import app.vibecast.presentation.TAG
import app.vibecast.presentation.permissions.LocationPermissionState
import app.vibecast.presentation.screens.main_screen.weather.LocationModel
import app.vibecast.presentation.screens.main_screen.weather.LocationWeatherModel
import app.vibecast.presentation.screens.main_screen.weather.WeatherModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@HiltViewModel
class MainViewModel @Inject constructor(
    private val locationGetter: LocationGetter,
    private val weatherRepository: WeatherRepository,
    private val locationRepository: LocationRepository,
    private val dataStoreRepository: UnitPreferenceRepository,
) : ViewModel() {


    fun setUpLocationData() {
        viewModelScope.launch {
            locationRepository.getLocations().collect { locations ->
                withContext(Dispatchers.Main) { _locations.value = locations }

            }
        }
    }


    private val _currentWeather = MutableLiveData<LocationWeatherModel>()
    val currentWeather: LiveData<LocationWeatherModel> get() = _currentWeather.distinctUntilChanged()

    fun checkPermissionState() {
        viewModelScope.launch(Dispatchers.IO) {
            when (locationPermissionState.value) {
                LocationPermissionState.Granted -> handleLocationGranted()
                else -> handleLocationDenied()
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


    /**
     * Queries repository to fetch weather data based on users last known coordinates
     * fetches weather data for default location if location couldnt be determined
     */
    @SuppressLint("MissingPermission")
    private suspend fun handleLocationGranted() {
        locationGetter.client.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            fetchWeatherData(location.latitude, location.longitude, _currentWeather)
                        } catch (e: Exception) {
                            // Handle specific error for fetching weather data
                            handleError(e)
                        }
                    }
                } else {
                    // location is null, show default location
                    viewModelScope.launch(Dispatchers.IO) {
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


    private suspend fun fetchWeatherData(
        lat: Double,
        lon: Double,
        weatherLiveDataObj: MutableLiveData<LocationWeatherModel>,
    ) {
        try {
            weatherRepository.getWeather(lat, lon).collect { weatherData ->
                updateWeatherData(weatherData, weatherLiveDataObj)
            }
        } catch (e: Exception) {
            handleError(e)
        }
    }


//    private suspend fun fetchWeatherData(
//        cityName: String,
//        weatherLiveDataObj: MutableLiveData<LocationWeatherModel>,
//    ) {
//        try {
//            weatherRepository.getWeather(cityName).collect { weatherData ->
//                updateWeatherData(weatherData, weatherLiveDataObj)
//            }
//        } catch (e: Exception) {
//            handleError(e)
//        }
//    }

    /**
     * Updates appropriate livedata object once new weather data has been fetched for it
     */
    private suspend fun updateWeatherData(
        locationWithWeatherDataDto: LocationWithWeatherDataDto,
        weatherLiveDataObj: MutableLiveData<LocationWeatherModel>,
    ) {

        try {
            val weatherData = convertWeatherDtoToWeatherModel(locationWithWeatherDataDto.weather)
            val locationData = LocationModel(
                locationWithWeatherDataDto.location.city,
                locationWithWeatherDataDto.location.country
            )
            withContext(Dispatchers.Main) {
                weatherLiveDataObj.value =
                    LocationWeatherModel(location = locationData, weather = weatherData)
                setCurrLocation(locationData)
            }
        } catch (e: Exception) {
            handleError(e)
        }
    }



    private suspend fun setSearchedWeatherData(
        locationWithWeatherDataDto: LocationWithWeatherDataDto,
        weatherLiveDataObj: MutableLiveData<LocationWeatherModel?>,
    ) {

        try {
            val weatherData = convertWeatherDtoToWeatherModel(locationWithWeatherDataDto.weather)
            val locationData = LocationModel(
                locationWithWeatherDataDto.location.city,
                locationWithWeatherDataDto.location.country
            )
            withContext(Dispatchers.Main) {
                weatherLiveDataObj.value =
                    LocationWeatherModel(location = locationData, weather = weatherData)
                setCurrLocation(locationData)
            }
        } catch (e: Exception) {
            handleError(e)
        }
    }

    private fun getDefaultLocation(): String {
        return "Chicago"
    }


    private val _searchedWeather = MutableLiveData<LocationWeatherModel?>()
    val searchedWeather: LiveData<LocationWeatherModel?> get() = _searchedWeather.distinctUntilChanged()


    fun getSearchedLocationWeather(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                weatherRepository.getSearchedWeather(query).collect { data ->
                    try {
                        withContext(Dispatchers.Main) {
                            if (data != null) {
                                Timber.tag("search").d(data.toString())
                                _emptySearchResponse.value=false
                                setSearchedWeatherData(data, _searchedWeather)
                                setCurrLocation(
                                    LocationModel(
                                        data.location.city,
                                        data.location.country
                                    )
                                )

                            } else {
                                _searchedWeather.value = currentWeather.value
                                _emptySearchResponse.value=true
                            }
                        }
                    } catch (e: Exception) {
                        Timber.tag(WEATHER_ERROR).d("$query viewmodel")
                        throw e
                    }
                }
            } catch (e: Exception) {
                Timber.tag(WEATHER_ERROR).d("$query viewmodel")
                throw e
            }
        }
    }



    private val _emptySearchResponse = MutableLiveData<Boolean>()
    val emptySearchResponse: LiveData<Boolean> get() = _emptySearchResponse

    private val _savedWeather = MutableLiveData<LocationWeatherModel>()
    val savedWeather: LiveData<LocationWeatherModel> get() = _savedWeather.distinctUntilChanged()


    fun getSavedLocationWeather() {
        viewModelScope.launch {
            try {
                locationRepository.getLocations().collect { locations ->
                    if (locationIndex.value != null && locations.isNotEmpty()) {
                        try {
                            weatherRepository.getWeather(locations[locationIndex.value!!].cityName)
                                .distinctUntilChanged()
                                .collect { data ->
                                    try {
//                                        val weatherData = convertWeatherDtoToWeatherModel(data.weather)
//                                        val location = LocationModel(
//                                            data.location.cityName,
//                                            data.location.country
//                                        )
//                                        _savedWeather.value = LocationWeatherModel(
//                                            location = location,
//                                            weather = weatherData
//                                        )
                                        updateWeatherData(data, _savedWeather)
                                        setCurrLocation(
                                            LocationModel(
                                                data.location.city,
                                                data.location.country
                                            )
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


    private var _locations = MutableLiveData<List<LocationModel>>()
    val locations: LiveData<List<LocationModel>> get() = _locations


    private var _currentLocation = MutableLiveData<LocationModel>()
    val currentLocation: LiveData<LocationModel> get() = _currentLocation.distinctUntilChanged()

    private fun setCurrLocation(location: LocationModel) {
        _currentLocation.value = location
    }


    fun addLocation(location: LocationModel) {
        locationRepository.addLocation(LocationDto(location.cityName, location.country))
    }

    fun deleteLocation(location: LocationModel) {
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

    fun resetIndex() {
        _locationIndex.value = 0
    }


    private suspend fun fetchDefaultWeatherData() {
        val defaultLocation = getDefaultLocation()
        try {
            weatherRepository.getWeather(defaultLocation).collect { weatherData ->
                updateWeatherData(weatherData, _currentWeather)
            }
        } catch (e: Exception) {
            handleError(e)
        }
    }


    private fun handleError(e: Exception) {
        Timber.tag(TAG).e("Error: $e")
        throw e
    }


    /**
     *  Converts Unix timestamp in seconds to AM PM format
     */
    private fun convertUnixTimestamp(unixTimestamp: Long, timeZoneId: String): String {
        val date = Date(unixTimestamp * 1000L)
        val sdf = SimpleDateFormat("ha", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone(timeZoneId)
        return sdf.format(date)
    }

    /**
     * Formats chance of rain to percentage between 0 and 100
     */
    private fun formatProp(prop: Double): Int = (prop * 100).toInt()


    /**
     * Formats UV index to number between 0 and 10
     */
    private fun formatUvi(uvi: Double): Double = uvi * 10


    /**
     * Formats visibility based on preferred measurement (imperial/metric)
     */
    private suspend fun formatVisibility(visibility: Int): String =
        suspendCoroutine { continuation ->
            var formattedVisibility: String

            viewModelScope.launch {
                when (dataStoreRepository.getPreference()) {
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

                }

                continuation.resume(formattedVisibility)
            }
        }

    /**
     * Formats wind speed based on preferred measurement (imperial/metric)
     */
    private fun formatWindSpeed(ws: Double): String {
        var formattedWs = String.format("%.1f mph", ws)
        viewModelScope.launch {
            formattedWs = when (dataStoreRepository.getPreference()) {
                Unit.IMPERIAL -> String.format("%.1f mph", ws)
                Unit.METRIC -> String.format("%.1f kmh", ws)
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
            currentWeather = weatherDto.currentWeather?.let {
                convertCurrentWeather(
                    it,
                    weatherDto.timezone
                )
            },
            hourlyWeather = weatherDto.hourlyWeather?.map {
                convertHourlyWeather(
                    it,
                    weatherDto.timezone
                )
            }
        )
    }

    private suspend fun convertCurrentWeather(
        dto: CurrentWeather,
        timeZoneId: String,
    ): WeatherModel.CurrentWeather {
        return WeatherModel.CurrentWeather(
            timestamp = convertUnixTimestamp(dto.timestamp, timeZoneId),
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

    private fun convertHourlyWeather(
        dto: HourlyWeather,
        timeZoneId: String,
    ): WeatherModel.HourlyWeather {
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
