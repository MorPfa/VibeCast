package app.vibecast.data.remote.source

import android.util.Log
import app.vibecast.BuildConfig
import app.vibecast.data.TAGS.WEATHER_ERROR
import app.vibecast.data.data_repository.data_source.remote.RemoteWeatherDataSource
import app.vibecast.data.data_repository.repository.Unit
import app.vibecast.data.remote.network.weather.CityApiModel
import app.vibecast.data.remote.network.weather.CoordinateApiModel
import app.vibecast.data.remote.network.weather.CurrentWeatherRemote
import app.vibecast.data.remote.network.weather.HourlyWeatherRemote
import app.vibecast.data.remote.network.weather.WeatherApiModel
import app.vibecast.data.remote.network.weather.WeatherConditionRemote
import app.vibecast.data.remote.network.weather.WeatherService
import app.vibecast.data.remote.source.RemoteImageDataSourceImpl.EmptyApiResponseException
import app.vibecast.domain.entity.CurrentWeather
import app.vibecast.domain.entity.HourlyWeather
import app.vibecast.domain.entity.LocationDto
import app.vibecast.domain.entity.LocationWithWeatherDataDto
import app.vibecast.domain.entity.WeatherCondition
import app.vibecast.domain.entity.WeatherDto
import app.vibecast.domain.repository.DataStoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject


class RemoteWeatherDataSourceImpl @Inject constructor(
    private val weatherService: WeatherService,
    private val dataStoreRepository: DataStoreRepository
) : RemoteWeatherDataSource {


    override fun getCoordinates(name: String): Flow<CoordinateApiModel> = flow {
        try {
            val coordinatesList = withContext(Dispatchers.IO) {
                weatherService.getCiyCoordinates(name, 1, BuildConfig.OWM_KEY)
            }
            if (coordinatesList.isNotEmpty()) {
                val firstCoordinate = coordinatesList[0]
                emit(firstCoordinate)
            } else {
                throw EmptyApiResponseException("Coordinates list is empty.")
            }
        }
        catch (e: HttpException) {
            throw WeatherFetchException("HTTP error fetching weather", e)
        }
        catch (e: Exception) {
            throw e
        }
    }


    override fun getCity(lat: Double, lon: Double): Flow<CityApiModel> = flow {
        try {
            val locationInfo = weatherService.getCiyName(lat, lon, 1, BuildConfig.OWM_KEY)
            if (locationInfo.isNotEmpty()){
                emit(locationInfo[0])
            }
        }
        catch (e: HttpException) {
            Log.e(WEATHER_ERROR, "$e")
            throw WeatherFetchException("HTTP error fetching weather data", e)
        }
        catch (e: Exception) {
            Log.e(WEATHER_ERROR, "$e in Datasource")
            throw WeatherFetchException("Error fetching city for lat $lat lon $lon", e)
        }
    }.flowOn(Dispatchers.IO)

    override fun getWeather(cityName: String): Flow<LocationWithWeatherDataDto> = flow {
        getCoordinates(cityName).collect{
            try {
                val weatherData : WeatherApiModel
                val preferredUnit = dataStoreRepository.getUnit()
                weatherData = when(preferredUnit){
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
                    weatherData.toWeatherDto()
                )
                emit(combinedData)
            }
            catch (e: HttpException) {
                Log.e(WEATHER_ERROR, "$e in DataSource")
                throw WeatherFetchException("HTTP error fetching weather data", e)
            }
            catch (e: Exception) {
                Log.e(WEATHER_ERROR, "$e in DataSource")
                throw WeatherFetchException("Error fetching weather data for $cityName", e)
            }
        }
    }.flowOn(Dispatchers.IO)




    override fun getWeather(lat: Double, lon: Double): Flow<LocationWithWeatherDataDto> = flow {
        try {
            val weatherData: WeatherApiModel
            val preferredUnit = dataStoreRepository.getUnit()
            weatherData = when (preferredUnit) {
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
            emit(LocationWithWeatherDataDto(LocationDto(
                weatherData.cityName, ""),
                weatherData.toWeatherDto()))
        } catch (e: HttpException) {
            Log.e(WEATHER_ERROR, "$e in DataSource")
            throw WeatherFetchException("HTTP error fetching weather data", e)
        }
        catch (e: Exception) {
            Log.e(WEATHER_ERROR, "$e in DataSource")
            throw WeatherFetchException("Error fetching weather data", e)
        }
    }.flowOn(Dispatchers.IO)



    class WeatherFetchException(message: String, cause: Throwable? = null) : Exception(message, cause)

    companion object {
    //Converting Api response data to domain layer entity
    fun WeatherApiModel.toWeatherDto(): WeatherDto {
        return WeatherDto(
            cityName =cityName,
            latitude = latitude,
            longitude = longitude,
            dataTimestamp = System.currentTimeMillis(),
            timezone = timezone,
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





