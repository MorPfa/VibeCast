package app.vibecast.data.remote.source

import app.vibecast.data.data_repository.data_source.remote.RemoteWeatherDataSource
import app.vibecast.data.remote.network.weather.CoordinateApiModel
import app.vibecast.data.remote.network.weather.CurrentWeatherRemote
import app.vibecast.data.remote.network.weather.HourlyWeatherRemote
import app.vibecast.data.remote.network.weather.WeatherApiModel
import app.vibecast.data.remote.network.weather.WeatherConditionRemote
import app.vibecast.data.remote.network.weather.WeatherService
import app.vibecast.domain.entity.CurrentWeather
import app.vibecast.domain.entity.HourlyWeather
import app.vibecast.domain.entity.Weather
import app.vibecast.domain.entity.WeatherCondition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RemoteWeatherDataSourceImpl @Inject constructor(private val weatherService: WeatherService) : RemoteWeatherDataSource {


    override fun getCity(): Flow<CoordinateApiModel> = flow{
        emit(weatherService.getCiyCoordinates("stub",1,"stub"))
        //So far only using placeholder values
        //TODO add real values / figure out where or how to pass them
    }

    override fun getWeather(): Flow<Weather> = flow {
        //So far only using placeholder values
        //TODO add real values / figure out where or how to pass them
        emit(weatherService.getWeather(1.0,1.0,"stub"))
    }.map {weatherApiModel ->
        weatherApiModel.toWeather()
        //TODO throw exception in case of error
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





