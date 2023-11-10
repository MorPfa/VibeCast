package app.vibecast.domain.usecase

import app.vibecast.data.local.db.location.LocationWithWeatherData
import app.vibecast.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

class AddLocationUseCase @Inject constructor(
    configuration: Configuration,
    private val locationRepository: LocationRepository
) : UseCase<AddLocationUseCase.Request, AddLocationUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = flow {
            emit(locationRepository.addLocationWeather(request.location))
        }.transform {
        emit(Response.Success)
        }

    data class Request(val location: LocationWithWeatherData) : UseCase.Request
    sealed class Response : UseCase.Response {
        data object Success : Response()

    }
}

