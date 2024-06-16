package app.vibecast.data.remote_data.data_source.weather

import app.vibecast.BuildConfig
import app.vibecast.data.remote_data.data_source.weather.RemoteWeatherDataSourceImpl.Companion.toWeatherDto
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
import app.vibecast.domain.util.Resource
import app.vibecast.domain.util.TAGS.WEATHER_ERROR
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

//    override suspend getCity(lat: Double, lon: Double): Resource<CityApiModel>  {
//        try {
//            val locationInfo = weatherService.getCiyName(lat, lon, 1, BuildConfig.OWM_KEY)
//            if (locationInfo.isNotEmpty()) {
//                emit(locationInfo[0])
//            }
//        } catch (e: HttpException) {
//            Timber.tag(WEATHER_ERROR).e(e)
//            throw WeatherFetchException("HTTP error fetching weather data", e)
//        } catch (e: Exception) {
//            Timber.tag(WEATHER_ERROR).e("$e in Datasource")
//            throw WeatherFetchException("Error fetching city for lat $lat lon $lon", e)
//        }
//    }.flowOn(Dispatchers.IO)

    override suspend fun getCity(lat: Double, lon: Double): Resource<CityApiModel> {
        return try {
            val response = weatherService.getCiyName(lat, lon, 1, BuildConfig.OWM_KEY)
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    Resource.Success(responseBody.first())
                } else {
                    Resource.Error("Response body is null")
                }
            } else {
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage)
        }
    }

    override suspend fun getCoordinates(name: String): Resource<CoordinateApiModel> {
        return try {
            val response = weatherService.getCiyCoordinates(name, 1, BuildConfig.OWM_KEY)
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    Resource.Success(responseBody.first())
                } else {
                    Resource.Error("Response body is null")
                }
            } else {
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage)
        }
    }

    override suspend fun getSearchCoordinates(name: String): Resource<CoordinateApiModel> {
        return try {
            val response = weatherService.getCiyCoordinates(name, 1, BuildConfig.OWM_KEY)
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    Resource.Success(responseBody.first())
                } else {
                    Resource.Error("Response body is null")
                }
            } else {
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage)
        }
    }

    override suspend fun getSearchedWeather(cityName: String): Resource<LocationWithWeatherDataDto> {
        return try {
            val correctUnit = chooseUnit()
            when (val coordinates = getSearchCoordinates(cityName)) {
                is Resource.Success -> {
                    val response = weatherService.getWeather(
                        coordinates.data?.latitude!!,
                        coordinates.data.longitude,
                        "minutely,daily,alerts",
                        correctUnit,
                        BuildConfig.OWM_KEY
                    )
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            val formattedDto = LocationWithWeatherDataDto(
                                LocationDto(coordinates.data.name, coordinates.data.country),
                                body.toWeatherDto(preferredUnit)
                            )
                            Resource.Success(data = formattedDto)
                        } else {
                            Resource.Error("Response body is empty")
                        }
                    } else {
                        Resource.Error(message = response.message())
                    }
                }

                is Resource.Error -> {
                    Resource.Error(message = coordinates.message)
                }
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage)
        }
    }


    override suspend fun getWeather(cityName: String): Resource<LocationWithWeatherDataDto> {
        return try {
            val correctUnit = chooseUnit()
            when (val coordinates = getCoordinates(cityName)) {
                is Resource.Success -> {
                    val response = weatherService.getWeather(
                        coordinates.data?.latitude!!,
                        coordinates.data.longitude,
                        "minutely,daily,alerts",
                        correctUnit,
                        BuildConfig.OWM_KEY
                    )
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            val formattedDto = LocationWithWeatherDataDto(
                                LocationDto(coordinates.data.name, coordinates.data.country),
                                body.toWeatherDto(preferredUnit)
                            )
                            Resource.Success(formattedDto)
                        } else {
                            Resource.Error("Response body is empty")
                        }
                    } else {
                        Resource.Error(message = response.message())
                    }
                }

                is Resource.Error -> {
                    Resource.Error(message = coordinates.message)
                }
            }
        } catch (e: Exception) {
            Timber.tag(WEATHER_ERROR).e("$e in DataSource")
            Resource.Error(e.localizedMessage)
        }
    }

    private suspend fun chooseUnit(): String {
        preferredUnit = dataStoreRepository.getPreference()
        return when (preferredUnit) {
            Unit.IMPERIAL -> "imperial"

            Unit.METRIC -> "metric"

            else -> "imperial"
        }
    }


    override suspend fun getWeather(
        lat: Double,
        lon: Double,
    ): Resource<LocationWithWeatherDataDto> {
        return try {
            val correctUnit = chooseUnit()
            val response = weatherService.getWeather(
                lat,
                lon,
                "minutely,daily,alerts",
                correctUnit,
                BuildConfig.OWM_KEY
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(
                        LocationWithWeatherDataDto(
                            LocationDto(
                                body.cityName, ""
                            ),
                            body.toWeatherDto(preferredUnit)
                        )
                    )
                } else {
                    Resource.Error("Response body is empty")
                }
            } else {
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Timber.tag(WEATHER_ERROR).e("$e in DataSource")
            Resource.Error(e.localizedMessage)
        }
    }


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





