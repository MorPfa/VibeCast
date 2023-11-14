package app.vibecast.domain.usecase

import app.vibecast.domain.entity.ImageDto
import app.vibecast.domain.repository.ImageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetImageUseCase @Inject constructor(
    configuration: Configuration,
    private val imageRepository: ImageRepository
) : UseCase<GetImageUseCase.Request , GetImageUseCase.Response>(configuration){

    override fun process(request: Request): Flow<Response> =
        imageRepository.pickRandomImage(request.query)
            .map {
                Response(it)
            }

    data class Request(val query: String) : UseCase.Request
    data class Response(val image: ImageDto?) : UseCase.Response
}