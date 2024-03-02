package app.vibecast.data.remote_data.data_source.weather

import android.util.Log
import app.vibecast.BuildConfig
import app.vibecast.domain.util.TAGS.WEATHER_ERROR
import app.vibecast.domain.repository.weather.Unit
import app.vibecast.data.remote_data.network.weather.model.CityApiModel
import app.vibecast.data.remote_data.network.weather.model.CoordinateApiModel
import app.vibecast.data.remote_data.network.weather.model.CurrentWeatherRemote
import app.vibecast.data.remote_data.network.weather.model.HourlyWeatherRemote
import app.vibecast.data.remote_data.network.weather.model.WeatherApiModel
import app.vibecast.data.remote_data.network.weather.model.WeatherConditionRemote
import app.vibecast.data.remote_data.network.weather.api.WeatherService
import app.vibecast.domain.model.CurrentWeather
import app.vibecast.domain.model.HourlyWeather
import app.vibecast.domain.model.LocationDto
import app.vibecast.domain.model.LocationWithWeatherDataDto
import app.vibecast.domain.model.WeatherCondition
import app.vibecast.domain.model.WeatherDto
import app.vibecast.domain.repository.weather.UnitPreferenceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject


class RemoteWeatherDataSourceImpl @Inject constructor(
    private val weatherService: WeatherService,
    private val dataStoreRepository: UnitPreferenceRepository,
) : RemoteWeatherDataSource {


    private var preferredUnit: Unit? = null

    /**
     *  Fetches city name based on coordinates using reverse geo coding API endpoint
     *  when data needs to be fetched based on users current location
     */
    override fun getCity(lat: Double, lon: Double): Flow<CityApiModel> = flow {
        try {
            val locationInfo = weatherService.getCiyName(lat, lon, 1, BuildConfig.OWM_KEY)
            if (locationInfo.isNotEmpty()) {
                emit(locationInfo[0])
            }
        } catch (e: HttpException) {
            Log.e(WEATHER_ERROR, "$e")
            throw WeatherFetchException("HTTP error fetching weather data", e)
        } catch (e: Exception) {
            Log.e(WEATHER_ERROR, "$e in Datasource")
            throw WeatherFetchException("Error fetching city for lat $lat lon $lon", e)
        }
    }.flowOn(Dispatchers.IO)


    /**
     *  Fetches list of coordinates based on city name using geo coding API endpoint
     *  when data needs to be fetched based on searched or saved location
     */
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

    /**
     *  Fetches weather data based on city name and preferred measurements
     */
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
                    Log.e(WEATHER_ERROR, "$e in DataSource")
                    throw WeatherFetchException("HTTP error fetching weather data", e)
                } catch (e: Exception) {
                    Log.e(WEATHER_ERROR, "$e in DataSource")
                    throw WeatherFetchException("Error fetching weather data for $cityName", e)
                }
            }
    }.flowOn(Dispatchers.IO)


    /**
     *  Fetches weather data based on coordinates and preferred measurements
     */
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
            Log.e(WEATHER_ERROR, "$e in DataSource")
            throw WeatherFetchException("HTTP error fetching weather data", e)
        } catch (e: Exception) {
            Log.e(WEATHER_ERROR, "$e in DataSource")
            throw WeatherFetchException("Error fetching weather data", e)
        }
    }.flowOn(Dispatchers.IO)


    class WeatherFetchException(message: String, cause: Throwable? = null) :
        Exception(message, cause)



    companion object {
        /**
         *  Extension function to convert an API response model to
         *  a Data Transfer Object to be used in the domain layer
         */
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





