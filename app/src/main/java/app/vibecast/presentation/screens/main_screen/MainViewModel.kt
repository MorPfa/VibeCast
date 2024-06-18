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
import app.vibecast.domain.model.WeatherCondition
import app.vibecast.domain.model.WeatherDto
import app.vibecast.domain.repository.weather.LocationRepository
import app.vibecast.domain.repository.weather.Unit
import app.vibecast.domain.repository.weather.UnitPreferenceRepository
import app.vibecast.domain.repository.weather.WeatherRepository
import app.vibecast.domain.util.LocationGetter
import app.vibecast.domain.util.Resource
import app.vibecast.presentation.TAG
import app.vibecast.presentation.permissions.LocationPermissionState
import app.vibecast.presentation.screens.main_screen.weather.LocationModel
import app.vibecast.presentation.screens.main_screen.weather.LocationWeatherModel
import app.vibecast.presentation.screens.main_screen.weather.WeatherModel
import app.vibecast.presentation.state.LocationWeatherState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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


    private val _currentWeather = MutableLiveData<LocationWeatherState>()
    val currentWeather: LiveData<LocationWeatherState> get() = _currentWeather.distinctUntilChanged()


    private val _locationPermissionState =
        MutableStateFlow<LocationPermissionState>(LocationPermissionState.Granted)
    private val locationPermissionState: StateFlow<LocationPermissionState> =
        _locationPermissionState


    private val _searchedWeather = MutableLiveData<LocationWeatherState>()
    val searchedWeather: LiveData<LocationWeatherState> get() = _searchedWeather.distinctUntilChanged()


    private val _savedWeather = MutableLiveData<LocationWeatherState>()
    val savedWeather: LiveData<LocationWeatherState> get() = _savedWeather.distinctUntilChanged()


    private var _locations = MutableLiveData<List<LocationModel>>()
    val locations: LiveData<List<LocationModel>> get() = _locations


    private var _currentLocation = MutableLiveData<LocationModel>()
    val currentLocation: LiveData<LocationModel> get() = _currentLocation.distinctUntilChanged()


    private var _locationIndex = MutableLiveData(0)
    val locationIndex: LiveData<Int> get() = _locationIndex


    fun setUpLocationData() {
        viewModelScope.launch {
            when (val locations = locationRepository.getLocations()) {
                is Resource.Success -> {
                    withContext(Dispatchers.Main) { _locations.value = locations.data!! }
                }

                is Resource.Error -> {
                    withContext(Dispatchers.Main) { _locations.value = emptyList() }
                }
            }
        }
    }


    fun checkPermissionState() {
        viewModelScope.launch(Dispatchers.IO) {
            when (locationPermissionState.value) {
                LocationPermissionState.Granted -> handleLocationGranted()
                else -> handleLocationDenied()
            }
        }
    }


    fun updatePermissionState(state: LocationPermissionState) {
        _locationPermissionState.value = state
    }


    /**
     * Queries repository to fetch weather data based on users last known coordinates
     * fetches weather data for default location if location couldn't be determined
     */
    @SuppressLint("MissingPermission")
    private fun handleLocationGranted() {
        locationGetter.client.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            fetchWeatherData(location.latitude, location.longitude, _currentWeather)
                        } catch (e: Exception) {
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

    private fun handleLocationDenied() {
        try {
            fetchDefaultWeatherData()
        } catch (e: Exception) {
            handleError(e)
        }
    }


    private fun fetchWeatherData(
        lat: Double,
        lon: Double,
        weatherLiveDataObj: MutableLiveData<LocationWeatherState>,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = weatherRepository.getWeather(lat, lon)) {
                is Resource.Success -> {
                    val weatherData = convertWeatherDtoToWeatherModel(result.data?.weather!!)
                    val locationData = LocationModel(
                        result.data.weather.cityName,
                        result.data.weather.country
                    )
                    updateWeatherData(
                        LocationWeatherState(
                            combinedData = LocationWeatherModel(
                                location = locationData,
                                weather = weatherData
                            )
                        ), weatherLiveDataObj
                    )
                    setCurrLocation(locationData)
                }

                is Resource.Error -> {
                    updateWeatherData(
                        LocationWeatherState(error = result.message!!),
                        weatherLiveDataObj
                    )
                }
            }
        }

    }

    /**
     * Updates appropriate livedata object once new weather data has been fetched for it
     */
    private suspend fun updateWeatherData(
        state: LocationWeatherState,
        weatherLiveDataObj: MutableLiveData<LocationWeatherState>,
    ) {
        withContext(Dispatchers.Main) {
            weatherLiveDataObj.value = state
        }
    }

    private fun getDefaultLocation(): String {
        return "Chicago"
    }


    fun getSearchedLocationWeather(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = weatherRepository.getSearchedWeather(query)) {
                is Resource.Success -> {
                    val weatherData = convertWeatherDtoToWeatherModel(result.data?.weather!!)
                    val locationData = LocationModel(
                        result.data.location.city,
                        result.data.location.country
                    )
                    val formattedWeatherData = LocationWeatherState(
                        combinedData = LocationWeatherModel(
                            location = locationData,
                            weather = weatherData
                        )
                    )
                    updateWeatherData(formattedWeatherData, _searchedWeather)
                    setCurrLocation(locationData)

                }
                is Resource.Error -> {
                    withContext(Dispatchers.Main) {
                        _searchedWeather.value =
                            currentWeather.value?.copy(error = "Could not fetch weather data for this location")
                    }
                }
            }
        }
    }


    fun getSavedLocationWeather() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val weatherResult =
                weatherRepository.getWeather(locations.value!![locationIndex.value!!].cityName)) {
                is Resource.Success -> {
                    val weatherDataModel =
                        convertWeatherDtoToWeatherModel(weatherResult.data?.weather!!)
                    val locationData = LocationModel(
                        weatherResult.data.location.city,
                        weatherResult.data.location.country
                    )
                    val formattedWeatherData =
                        LocationWeatherModel(location = locationData, weather = weatherDataModel)
                    updateWeatherData(
                        LocationWeatherState(combinedData = formattedWeatherData),
                        _savedWeather
                    )
                    setCurrLocation(
                        LocationModel(
                            weatherResult.data.location.city,
                            weatherResult.data.location.country
                        )
                    )

                }

                is Resource.Error -> {
                    Timber.tag("WEATHER_REFORMAT")
                        .d("weatherResult error: ${weatherResult.message}")
                }
            }


        }
    }


    private suspend fun setCurrLocation(location: LocationModel) {
        withContext(Dispatchers.Main) {
            _currentLocation.value = location
        }

    }


    fun addLocation(location: LocationModel) {
        locationRepository.addLocation(LocationDto(location.cityName, location.country))
        _locations.value = locations.value?.plus(location)
    }

    fun deleteLocation(location: LocationModel) {
        locationRepository.deleteLocation(LocationDto(location.cityName, location.country))
        _locations.value = locations.value?.filter { it != location }
    }


    fun incrementIndex() {
        _locationIndex.value = _locationIndex.value?.plus(1)
    }

    fun decrementIndex() {
        _locationIndex.value = _locationIndex.value?.minus(1)
    }

    fun resetIndex() {
        _locationIndex.value = 0
    }


    private fun fetchDefaultWeatherData() {
        viewModelScope.launch(Dispatchers.IO) {
            val defaultLocation = getDefaultLocation()
            when (val result = weatherRepository.getWeather(defaultLocation)) {
                is Resource.Success -> {
                    val weatherData = convertWeatherDtoToWeatherModel(result.data?.weather!!)
                    val model = LocationWeatherModel(
                        location = LocationModel(
                            result.data.location.city,
                            result.data.location.country
                        ), weather = weatherData
                    )
                    updateWeatherData(LocationWeatherState(combinedData = model), _currentWeather)
                }

                is Resource.Error -> {
                    updateWeatherData(LocationWeatherState(error = result.message), _currentWeather)
                }
            }
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
