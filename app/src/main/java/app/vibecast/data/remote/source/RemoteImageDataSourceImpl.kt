package app.vibecast.data.remote.source

import app.vibecast.data.data_repository.data_source.remote.RemoteImageDataSource
import app.vibecast.data.remote.network.image.ImageApiModel
import app.vibecast.data.remote.network.image.ImageService
import app.vibecast.domain.entity.ImageDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import javax.inject.Inject

class RemoteImageDataSourceImpl @Inject constructor(
    private val imageService: ImageService) : RemoteImageDataSource {

    override fun getImages(query: String): Flow<ImageDto> = flow {
        try {
            val images = imageService.getImages(query, "portrait", 1, "high")
            if (images.isNotEmpty()) {
                emit(images[0].toImagesDto())
            } else {
                throw EmptyApiResponseException("Empty API response")
            }
        } catch (e: EmptyApiResponseException) {
            throw e  // Rethrow the same exception
        } catch (e: HttpException) {
            throw ImageFetchException("HTTP error fetching images", e)
        }
    }.flowOn(Dispatchers.IO)



    class EmptyApiResponseException(message: String) : Exception(message)
    class ImageFetchException(message: String, cause: Throwable? = null) : Exception(message, cause)

    private fun ImageApiModel.toImagesDto(): ImageDto {
        return ImageDto(
                id = this.id,
                description = this.description,
                altDescription = this.altDescription,
                urls = ImageDto.PhotoUrls(
                    full = this.urls.full,
                    regular = this.urls.regular,
                    small = this.urls.small,
                    thumb = this.urls.thumb
                ),
                user = ImageDto.UnsplashUser(
                    id = this.user.id,
                    name = this.user.name,
                    userName = this.user.userName,
                    portfolioUrl = this.user.portfolioUrl
                ),
                links = ImageDto.PhotoLinks(
                    user = this.links.user,
                    downloadLink =  this.links.downloadLink
                )
            )
        }
    }



