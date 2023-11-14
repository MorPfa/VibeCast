package app.vibecast.domain.usecase

import app.vibecast.domain.entity.ImageDto
import app.vibecast.domain.repository.ImageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetGalleryUseCase @Inject constructor(
    configuration: Configuration,
    private val imageRepository: ImageRepository
) : UseCase<GetGalleryUseCase.Request , GetGalleryUseCase.Response>(configuration){

    override fun process(request: Request): Flow<Response> =
        imageRepository.pickRandomImage(request.query)
            .map {
                Response(it)
            }

    //TODO add mechanism to add image to db and retrieve list of saved images, possibly using paging3

    data class Request(val query: String) : UseCase.Request
    data class Response(val image: ImageDto?) : UseCase.Response
}