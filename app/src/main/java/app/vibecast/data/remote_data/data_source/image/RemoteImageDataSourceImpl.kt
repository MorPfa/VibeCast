package app.vibecast.data.remote_data.data_source.image

import app.vibecast.data.remote_data.network.image.model.ImageApiModel
import app.vibecast.data.remote_data.network.image.api.ImageService
import app.vibecast.domain.model.ImageDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import javax.inject.Inject

class RemoteImageDataSourceImpl @Inject constructor(
    private val imageService: ImageService,
) : RemoteImageDataSource {

    /**
     *  Fetches list of random images that match query string in some way
     */
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

    /**
     *  Fetches the specific download URL for specified image which is necessary
     *  due to the Unsplash API Guidelines
     */
    override fun getImageForDownload(query: String): Flow<String> = flow {
        val image = imageService.getImageForDownload(query).url
        emit(image)
    }

    class EmptyApiResponseException(message: String) : Exception(message)
    class ImageFetchException(message: String, cause: Throwable? = null) : Exception(message, cause)


    /**
     *  Converts API response model to Data Transfer Object
     */
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
                downloadLink = this.links.downloadLink
            ),
            timestamp = null
        )
    }
}



