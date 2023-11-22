package app.vibecast.data.local.source

import app.vibecast.data.data_repository.data_source.local.LocalImageDataSource
import app.vibecast.data.local.db.image.ImageDao
import app.vibecast.data.local.db.image.ImageEntity
import app.vibecast.domain.entity.ImageDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalImageDataSourceImpl @Inject constructor(
    private val imageDao: ImageDao) : LocalImageDataSource {

    override fun getImages(): Flow<List<ImageDto>> = imageDao.getAllImages().map { imageList ->
        imageList.map { it.toImageDto() }
    }

    override suspend fun addImage(image: ImageDto) = imageDao.addImage(
        ImageEntity(image.id, image.description, image.altDescription ,image.urls.regular, image.user.name, image.user.id, image.user.userName, image.user.portfolioUrl, image.links.user, image.links.downloadLink)
    )

    override suspend fun deleteImage(image: ImageDto) = imageDao.deleteImage(
    ImageEntity(image.id, image.description, image.altDescription, image.urls.regular,image.user.name, image.user.id, image.user.userName, image.user.portfolioUrl, image.links.user, image.links.downloadLink)
    )


    private fun ImageEntity.toImageDto(): ImageDto {
        return ImageDto(
            id = this.id,
            description = this.description,
            altDescription = this.altDescription,
            urls = ImageDto.PhotoUrls(
                raw = "",
                full = "",
                regular = this.regularUrl,
                small = "",
                thumb = ""
            ),
            user = ImageDto.UnsplashUser(
                id = this.userId,
                name = this.name,
                userName = this.userName,
                portfolioUrl = this.portfolioUrl
            ), links = ImageDto.PhotoLinks(
                user = this.userLink,
                downloadLink = this.downloadLink
            )
        )
    }
}