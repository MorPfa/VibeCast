package app.vibecast.data.remote_data.data_source.weather

import app.vibecast.BuildConfig
import app.vibecast.data.remote_data.network.weather.api.WeatherService
import app.vibecast.data.remote_data.network.weather.model.CityApiModel
import app.vibecast.data.remote_data.network.weather.model.CoordinateApiModel
import app.vibecast.data.remote_data.network.weather.model.CurrentWeatherRemote
import app.vibecast.data.remote_data.network.weather.model.HourlyWeatherRemote
import app.vibecast.data.remote_data.network.weather.model.WeatherApiModel
import app.vibecast.data.remote_data.network.weather.model.WeatherConditionRemote
import app.vibecast.domain.model.CurrentWeather
import app.vibecast.domain.model.HourlyWeather
import app.vibecast.domain.model.LocationDto
import app.vibecast.domain.model.LocationWithWeatherDataDto
import app.vibecast.domain.model.WeatherCondition
import app.vibecast.domain.model.WeatherDto
import app.vibecast.domain.repository.weather.Unit
import app.vibecast.domain.repository.weather.UnitPreferenceRepository
import app.vibecast.domain.util.TAGS.WEATHER_ERROR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject


/**
 * Implementation of [RemoteWeatherDataSource]
 *
 * Methods:
 * - [getCity] Calls reverse geo coding endpoint to fetch city info for users coordinates
 * - [getCoordinates] Calls geo coding endpoint to fetch coordinates for specified city
 * - [getSearchCoordinates] Same as [getCoordinates] but specifically for search results since result needs to be nullable
 * - [getWeather] Fetches weather data based on coordinates and preferred measurements
 * - [getWeather] Fetches weather data based on city name and preferred measurements
 * - [toWeatherDto] Extension function to convert an API response model to a Data Transfer Object
 */

class RemoteWeatherDataSourceImpl @Inject constructor(
    private val weatherService: WeatherService,
    private val dataStoreRepository: UnitPreferenceRepository,
) : RemoteWeatherDataSource {


    private var preferredUnit: Unit? = null

    override fun getCity(lat: Double, lon: Double): Flow<CityApiModel> = flow {
        try {
            val locationInfo = weatherService.getCiyName(lat, lon, 1, BuildConfig.OWM_KEY)
            if (locationInfo.isNotEmpty()) {
                emit(locationInfo[0])
            }
        } catch (e: HttpException) {
            Timber.tag(WEATHER_ERROR).e(e)
            throw WeatherFetchException("HTTP error fetching weather data", e)
        } catch (e: Exception) {
            Timber.tag(WEATHER_ERROR).e("$e in Datasource")
            throw WeatherFetchException("Error fetching city for lat $lat lon $lon", e)
        }
    }.flowOn(Dispatchers.IO)

    override fun getCoordinates(name: String): Flow<CoordinateApiModel> = flow {
        try {
            val coordinatesList = withContext(Dispatchers.IO) {
                weatherService.getCiyCoordinates(name, 1, BuildConfig.OWM_KEY)
            }
            coordinatesList.firstOrNull()?.let {
                emit(it)
            }
        } catch (e: HttpException) {
            throw WeatherFetchException("HTTP error fetching weather", e)
        } catch (e: Exception) {
            throw e
        }
    }

    override fun getSearchCoordinates(name: String): Flow<CoordinateApiModel?> = flow {
        try {
            val coordinatesList = weatherService.getCiyCoordinates(name, 1, BuildConfig.OWM_KEY)
            emit(coordinatesList.firstOrNull())

        } catch (e: HttpException) {
            throw WeatherFetchException("HTTP error fetching weather", e)
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)

    override fun getSearchedWeather(cityName: String): Flow<LocationWithWeatherDataDto?> = flow {
        getSearchCoordinates(cityName)
            .collect { coordinate ->
                coordinate?.let {
                    try {
                        preferredUnit = dataStoreRepository.getPreference()
                        val weatherData: WeatherApiModel = when (preferredUnit) {
                            Unit.IMPERIAL -> weatherService.getWeather(
                                it.latitude,
                                it.longitude,
                                "minutely,daily,alerts",
                                "imperial",
                                BuildConfig.OWM_KEY
                            )

                            Unit.METRIC -> weatherService.getWeather(
                                it.latitude,
                                it.longitude,
                                "minutely,daily,alerts",
                                "metric",
                                BuildConfig.OWM_KEY
                            )

                            else -> {
                                weatherService.getWeather(
                                    it.latitude,
                                    it.longitude,
                                    "minutely,daily,alerts",
                                    "imperial",
                                    BuildConfig.OWM_KEY
                                )
                            }
                        }
                        val combinedData = LocationWithWeatherDataDto(
                            LocationDto(it.name, it.country),
                            weatherData.toWeatherDto(preferredUnit)
                        )
                        emit(combinedData)
                    } catch (e: Exception) {
                        Timber.tag(WEATHER_ERROR).e("$e in DataSource")
                    }
                } ?: emit(null)
            }
    }.flowOn(Dispatchers.IO)


    override fun getWeather(cityName: String): Flow<LocationWithWeatherDataDto> = flow {
        getCoordinates(cityName)
            .collect {
                try {
                    preferredUnit = dataStoreRepository.getPreference()
                    val weatherData: WeatherApiModel = when (preferredUnit) {
                        Unit.IMPERIAL -> weatherService.getWeather(
                            it.latitude,
                            it.longitude,
                            "minutely,daily,alerts",
                            "imperial",
                            BuildConfig.OWM_KEY
                        )

                        Unit.METRIC -> weatherService.getWeather(
                            it.latitude,
                            it.longitude,
                            "minutely,daily,alerts",
                            "metric",
                            BuildConfig.OWM_KEY
                        )

                        else -> {
                            weatherService.getWeather(
                                it.latitude,
                                it.longitude,
                                "minutely,daily,alerts",
                                "imperial",
                                BuildConfig.OWM_KEY
                            )
                        }
                    }
                    val combinedData = LocationWithWeatherDataDto(
                        LocationDto(it.name, it.country),
                        weatherData.toWeatherDto(preferredUnit)
                    )
                    emit(combinedData)
                } catch (e: HttpException) {
                    Timber.tag(WEATHER_ERROR).e("$e in DataSource")
                    throw WeatherFetchException("HTTP error fetching weather data", e)
                } catch (e: Exception) {
                    Timber.tag(WEATHER_ERROR).e("$e in DataSource")
                    throw WeatherFetchException("Error fetching weather data for $cityName", e)
                }
            }
    }.flowOn(Dispatchers.IO)


    override fun getWeather(lat: Double, lon: Double): Flow<LocationWithWeatherDataDto> = flow {
        try {
            preferredUnit = dataStoreRepository.getPreference()
            val weatherData: WeatherApiModel = when (preferredUnit) {
                Unit.IMPERIAL -> weatherService.getWeather(
                    lat,
                    lon,
                    "minutely,daily,alerts",
                    "imperial",
                    BuildConfig.OWM_KEY
                )

                Unit.METRIC -> weatherService.getWeather(
                    lat,
                    lon,
                    "minutely,daily,alerts",
                    "metric",
                    BuildConfig.OWM_KEY
                )

                else -> {
                    weatherService.getWeather(
                        lat,
                        lon,
                        "minutely,daily,alerts",
                        "imperial",
                        BuildConfig.OWM_KEY
                    )
                }
            }
            emit(
                LocationWithWeatherDataDto(
                    LocationDto(
                        weatherData.cityName, ""
                    ),
                    weatherData.toWeatherDto(preferredUnit)
                )
            )
        } catch (e: HttpException) {
            Timber.tag(WEATHER_ERROR).e("$e in DataSource")
            throw WeatherFetchException("HTTP error fetching weather data", e)
        } catch (e: Exception) {
            Timber.tag(WEATHER_ERROR).e("$e in DataSource")
            throw WeatherFetchException("Error fetching weather data", e)
        }
    }.flowOn(Dispatchers.IO)


    class WeatherFetchException(message: String, cause: Throwable? = null) :
        Exception(message, cause)


    companion object {
        fun WeatherApiModel.toWeatherDto(preferredUnit: Unit?): WeatherDto {
            return WeatherDto(
                cityName = cityName,
                country = "",
                latitude = latitude,
                longitude = longitude,
                dataTimestamp = System.currentTimeMillis(),
                timezone = timezone,
                unit = preferredUnit,
                currentWeather = currentWeatherRemote.toCurrentWeather(),
                hourlyWeather = hourlyWeather.map { it.toHourlyWeather() }
            )
        }

        private fun CurrentWeatherRemote.toCurrentWeather(): CurrentWeather {
            return CurrentWeather(
                timestamp = timestamp,
                temperature = temperature,
                feelsLike = feelsLike,
                humidity = humidity,
                uvi = uvi,
                cloudCover = cloudCover,
                visibility = visibility,
                windSpeed = windSpeed,
                weatherConditions = weatherConditionRemotes.map { it.toWeatherCondition() }
            )
        }

        private fun HourlyWeatherRemote.toHourlyWeather(): HourlyWeather {
            return HourlyWeather(
                timestamp = timestamp,
                temperature = temperature,
                feelsLike = feelsLike,
                humidity = humidity,
                uvi = uvi,
                windSpeed = windSpeed,
                weatherConditions = weatherConditionRemotes.map { it.toWeatherCondition() },
                chanceOfRain = chanceOfRain
            )
        }

        private fun WeatherConditionRemote.toWeatherCondition(): WeatherCondition {
            return WeatherCondition(
                mainDescription = mainDescription,
                icon = icon
            )
        }
    }

}





