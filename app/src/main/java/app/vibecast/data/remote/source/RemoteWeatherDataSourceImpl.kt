package app.vibecast.data.remote.source

import android.util.Log
import app.vibecast.BuildConfig
import app.vibecast.data.data_repository.data_source.remote.RemoteWeatherDataSource
import app.vibecast.data.remote.network.weather.CoordinateApiModel
import app.vibecast.data.remote.network.weather.CurrentWeatherRemote
import app.vibecast.data.remote.network.weather.HourlyWeatherRemote
import app.vibecast.data.remote.network.weather.WeatherApiModel
import app.vibecast.data.remote.network.weather.WeatherConditionRemote
import app.vibecast.data.remote.network.weather.WeatherService
import app.vibecast.domain.entity.CurrentWeather
import app.vibecast.domain.entity.HourlyWeather
import app.vibecast.domain.entity.UseCaseException
import app.vibecast.domain.entity.WeatherDto
import app.vibecast.domain.entity.WeatherCondition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import javax.inject.Inject

const val TAG = "DebugTag"

class RemoteWeatherDataSourceImpl @Inject constructor(
    private val weatherService: WeatherService,
) : RemoteWeatherDataSource {


    override fun getCoordinates(name: String): Flow<CoordinateApiModel> = flow {
        try {
            val coordinatesList = weatherService.getCiyCoordinates(name, 1, BuildConfig.OWM_KEY)

            if (coordinatesList.isNotEmpty()) {
                val firstCoordinate = coordinatesList[0]
                Log.d(TAG, "First Coordinate: $firstCoordinate")
                emit(firstCoordinate)
            } else {
                throw UseCaseException.WeatherException(Throwable("Coordinates list is empty."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting coordinates", e)
            throw UseCaseException.WeatherException(e)
        }
    }


    override fun getWeather(name: String): Flow<WeatherDto> = flow {
        val coordinates = getCoordinates(name).single()
        val weatherData = weatherService.getWeather(coordinates.latitude, coordinates.longitude ,"minutely,daily" ,BuildConfig.OWM_KEY)

        emit(weatherData)
    }.map {weatherApiModel ->
        weatherApiModel.toWeather()
    }.catch {
        throw UseCaseException.WeatherException(it)
    }

    override fun getWeather(lat : Double, lon : Double): Flow<WeatherDto> = flow {
        val weatherData = weatherService.getWeather(lat, lon, "minutely,daily", BuildConfig.OWM_KEY)
        emit(weatherData)
    }.map {weatherApiModel ->
        weatherApiModel.toWeather()
    }.catch {
        throw UseCaseException.WeatherException(it)
    }


    companion object {
    //Converting Api response data to domain layer entity
    fun WeatherApiModel.toWeather(): WeatherDto {
        return WeatherDto(
            cityName =cityName,
            latitude = latitude,
            longitude = longitude,
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
            cloudCover = cloudCover,
            windSpeed = windSpeed,
            weatherConditions = weatherConditionRemotes.map { it.toWeatherCondition() },
            chanceOfRain = chanceOfRain
        )
    }

    private fun WeatherConditionRemote.toWeatherCondition(): WeatherCondition {
        return WeatherCondition(
            conditionId = conditionId,
            mainDescription = mainDescription,
            detailedDescription = detailedDescription,
            icon = icon
        )
    }
}

}





