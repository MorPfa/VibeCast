package app.vibecast.domain.usecase

import app.vibecast.domain.entity.LocationDto
import app.vibecast.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetLocationUseCase @Inject constructor(
    configuration: Configuration,
    private val locationRepository: LocationRepository
) : UseCase<GetLocationUseCase.Request, GetLocationUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> =
        locationRepository.getLocation(request.cityName)
            .map {Response(it)
        }

    data class Request(val cityName: String) : UseCase.Request
    data class Response(val location: LocationDto) : UseCase.Response
}