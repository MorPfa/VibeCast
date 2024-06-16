package app.vibecast.data.remote_data.data_source.image


import app.vibecast.data.remote_data.network.image.api.ImageService
import app.vibecast.data.remote_data.network.image.model.ImageApiModel
import app.vibecast.domain.model.ImageDto
import app.vibecast.domain.util.Resource
import javax.inject.Inject


/**
 * Implementation of [RemoteImageDataSource]
 *
 * Methods:
 * - [getImages] Fetches list of random images that match query string and returns them as Image Data Transfer Objects.
 * - [getImageForDownload] Fetches the download URL for specified image which is necessary due to the Unsplash API Guidelines
 * - [toImagesDto] Converts API response model to Data Transfer Object
 */
class RemoteImageDataSourceImpl @Inject constructor(
    private val imageService: ImageService,
) : RemoteImageDataSource {

    override suspend fun getImages(query: String, collections: String): Resource<ImageDto> {
        return try {
            val response = imageService.getImages(query, "portrait", 1, "high", collections)
            if(response.isSuccessful){
                val body = response.body()
                if(body != null){
                    val firstImage = body[0].toImagesDto()
                    Resource.Success(data = firstImage)
                } else {
                    Resource.Error(message = "Response body is null")
                }
            } else {
                Resource.Error(message = response.message())
            }
        }catch (e : Exception){
            Resource.Error(message = e.message.toString())
        }
    }

    override suspend fun getImageForDownload(query: String): Resource<String> {
        return try {
            val response = imageService.getImageForDownload(query)
            if(response.isSuccessful){
                val body = response.body()
                if(body != null){
                    Resource.Success(data = body.url)
                } else {
                    Resource.Error(message = "Response body is null")
                }
            } else {
                Resource.Error(message = response.message())
            }
        }catch (e : Exception){
            Resource.Error(message = e.localizedMessage)
        }


    }

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



