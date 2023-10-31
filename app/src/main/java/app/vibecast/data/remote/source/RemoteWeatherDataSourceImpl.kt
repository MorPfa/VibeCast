package app.vibecast.data.remote.source

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
import app.vibecast.domain.entity.Weather
import app.vibecast.domain.entity.WeatherCondition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RemoteWeatherDataSourceImpl @Inject constructor(
    private val weatherService: WeatherService) : RemoteWeatherDataSource {


    override fun getCity(name : String): Flow<CoordinateApiModel> = flow{
        emit(weatherService.getCiyCoordinates(name,1,BuildConfig.OWM_KEY))

    }
    override fun getWeather(name: String): Flow<Weather> = flow {
        val coordinates = weatherService.getCiyCoordinates(name,1, BuildConfig.OWM_KEY)
        val weatherData = weatherService.getWeather(coordinates.latitude, coordinates.longitude, BuildConfig.OWM_KEY)
        emit(weatherData)
    }.map {weatherApiModel ->
        weatherApiModel.toWeather()
    }.catch {
        throw UseCaseException.WeatherException(it)
    }


    companion object {
    //Converting Api response data to domain layer entity
    fun WeatherApiModel.toWeather(): Weather {
        return Weather(
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





