package app.vibecast.domain.usecase

import app.vibecast.domain.entity.Weather
import app.vibecast.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class GetCurrentWeatherUseCase @Inject constructor(
    configuration: Configuration,
    private val weatherRepository: WeatherRepository
) : UseCase<GetCurrentWeatherUseCase.Request , GetCurrentWeatherUseCase.Response>(configuration){

    override fun process(request: Request): Flow<Response> =
        weatherRepository.getWeather(request.weatherDataCityName)
            .map {
                Response(it)
            }

    data class Request(val weatherDataCityName: String) : UseCase.Request
    data class Response(val weather: Weather) : UseCase.Response
}