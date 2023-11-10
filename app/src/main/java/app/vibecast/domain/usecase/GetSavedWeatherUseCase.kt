package app.vibecast.domain.usecase

import app.vibecast.domain.entity.Weather
import app.vibecast.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetSavedWeatherUseCase @Inject constructor(
    configuration: Configuration,
    private val locationRepository: LocationRepository
) : UseCase<GetSavedWeatherUseCase.Request , GetSavedWeatherUseCase.Response>(configuration){

    override fun process(request: Request): Flow<Response> =
        locationRepository.getLocationWeather(request.index)
            .map {
                Response(it)
            }

    data class Request(val index : Int) : UseCase.Request
    data class Response(val weather: Weather) : UseCase.Response
}