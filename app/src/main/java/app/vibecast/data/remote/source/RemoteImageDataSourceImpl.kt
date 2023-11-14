package app.vibecast.data.remote.source

import app.vibecast.data.data_repository.data_source.remote.RemoteImageDataSource
import app.vibecast.data.remote.network.image.ImageApiModel
import app.vibecast.data.remote.network.image.ImageService
import app.vibecast.domain.entity.ImageDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RemoteImageDataSourceImpl @Inject constructor(
    private val imageService: ImageService) : RemoteImageDataSource {

    override fun getImages(query: String): Flow<List<ImageDto>> = flow {
        emit(imageService.getImages(query, "portrait"))
    }.map {images -> images.toImagesList()
    }


    private fun ImageApiModel.toImagesList(): List<ImageDto> {
        return results.map { image ->
            ImageDto(
                id = image.id,
                description = image.description,
                urls = ImageDto.PhotoUrls(
                    raw = image.urls.raw,
                    full = image.urls.full,
                    regular = image.urls.regular,
                    small = image.urls.small,
                    thumb = image.urls.thumb
                ),
                user = ImageDto.UnsplashUser(
                    id = image.user.id,
                    name = image.user.name,
                    userName = image.user.userName,
                    portfolioUrl = image.user.portfolioUrl
                )
            )
        }
    }



}