package app.vibecast.data.remote.source

import app.vibecast.BuildConfig
import app.vibecast.data.data_repository.data_source.remote.RemoteWeatherDataSource
import app.vibecast.data.remote.network.weather.CityApiModel
import app.vibecast.data.remote.network.weather.CoordinateApiModel
import app.vibecast.data.remote.network.weather.CurrentWeatherRemote
import app.vibecast.data.remote.network.weather.HourlyWeatherRemote
import app.vibecast.data.remote.network.weather.WeatherApiModel
import app.vibecast.data.remote.network.weather.WeatherConditionRemote
import app.vibecast.data.remote.network.weather.WeatherService
import app.vibecast.domain.entity.CurrentWeather
import app.vibecast.domain.entity.HourlyWeather
import app.vibecast.domain.entity.UseCaseException
import app.vibecast.domain.entity.WeatherCondition
import app.vibecast.domain.entity.WeatherDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject


class RemoteWeatherDataSourceImpl @Inject constructor(
    private val weatherService: WeatherService,
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
                throw UseCaseException.WeatherException(Throwable("Coordinates list is empty."))
            }
        } catch (e: Exception) {
            throw UseCaseException.WeatherException(e)
        }
    }


    override fun getCity(lat: Double, lon: Double): Flow<CityApiModel> = flow {
        val weatherData = weatherService.getCiyName(lat, lon, 1, BuildConfig.OWM_KEY)[0]
        emit(weatherData)
    }.flowOn(Dispatchers.IO)

    override fun getWeather(name: String): Flow<WeatherDto> = flow {
        getCoordinates(name).collect{
            val weatherData = weatherService.getWeather(
                it.latitude, it.longitude, "minutely,daily,alerts", BuildConfig.OWM_KEY
            )
            weatherData.cityName = "${it.name} - ${it.country}"
            weatherData.hourlyWeather[1].temperature = kelvinToFahrenheit(weatherData.hourlyWeather[1].temperature)
            weatherData.hourlyWeather[2].temperature = kelvinToFahrenheit(weatherData.hourlyWeather[2].temperature)
            weatherData.currentWeatherRemote.temperature = kelvinToFahrenheit(weatherData.currentWeatherRemote.temperature)
            weatherData.currentWeatherRemote.feelsLike = kelvinToFahrenheit(weatherData.currentWeatherRemote.feelsLike)
            emit(weatherData.toWeather())
        }

    }.catch {
        throw UseCaseException.WeatherException(it)
    }.flowOn(Dispatchers.IO)



    override fun getWeather(lat: Double, lon: Double): Flow<WeatherDto> = flow {
        val weatherData = weatherService.getWeather(lat, lon, "minutely,daily,alerts", BuildConfig.OWM_KEY)
        weatherData.hourlyWeather[1].temperature = kelvinToFahrenheit(weatherData.hourlyWeather[1].temperature)
        weatherData.hourlyWeather[2].temperature = kelvinToFahrenheit(weatherData.hourlyWeather[2].temperature)
        weatherData.currentWeatherRemote.temperature = kelvinToFahrenheit(weatherData.currentWeatherRemote.temperature)
        weatherData.currentWeatherRemote.feelsLike = kelvinToFahrenheit(weatherData.currentWeatherRemote.feelsLike)
        emit(weatherData.toWeather())
    }.catch {
        throw UseCaseException.WeatherException(it)
    }.flowOn(Dispatchers.IO)




    private fun kelvinToFahrenheit(kelvin: Double): Double {
        val result = (kelvin - 273.15) * 9 / 5 + 32
        return BigDecimal(result).setScale(1, RoundingMode.HALF_UP).toDouble()
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





