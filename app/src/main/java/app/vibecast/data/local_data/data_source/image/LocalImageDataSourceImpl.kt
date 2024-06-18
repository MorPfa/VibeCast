package app.vibecast.data.local_data.data_source.image

import app.vibecast.data.local_data.db.image.dao.ImageDao
import app.vibecast.data.local_data.db.image.model.ImageEntity
import app.vibecast.domain.model.ImageDto
import app.vibecast.domain.util.Resource
import app.vibecast.domain.util.TAGS.COROUTINE_ERROR
import app.vibecast.domain.util.TAGS.DB_ERROR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Implementation of [LocalImageDataSource]
 *
 * Methods:
 * - [getImages] Queries database for all saved images and returns them as Image Data Transfer Objects.
 * - [addImage] Adds image to database after converting specified image DTO to DB Entity.
 * - [deleteImage] Deletes image from database after converting specified image DTO to DB Entity.
 * - [toImageDto] Converts Image Entity to Image Data Transfer Object.
 */
class LocalImageDataSourceImpl @Inject constructor(
    private val imageDao: ImageDao,
) : LocalImageDataSource {


    override fun getImages(): Flow<List<ImageDto>> = imageDao.getAllImages().map { imageList ->
        try {
            imageList.map { it.toImageDto() }
        } catch (e: Exception) {
            emptyList()

        }
    }


    override suspend fun addImage(image: ImageDto) {
        withContext(Dispatchers.IO) {
            try {
                ensureActive()
                imageDao.addImage(
                    ImageEntity(
                        image.id,
                        image.description,
                        image.altDescription,
                        image.urls.regular,
                        image.user.name,
                        image.user.id,
                        image.user.userName,
                        image.user.portfolioUrl,
                        image.links.user,
                        image.links.downloadLink,
                        System.currentTimeMillis()
                    )
                )

            } catch (e: CancellationException) {
                Timber.tag(COROUTINE_ERROR).e("Coroutine cancelled $e")
                throw e

            } catch (e: Exception) {
                Timber.tag(DB_ERROR).e("Error inserting image into db $e")
                throw e
            }
        }
    }

    override suspend fun deleteAllImages() {
        withContext(Dispatchers.IO) {
            try {
                ensureActive()
                imageDao.deleteAllImages()
            } catch (e: CancellationException) {
                Timber.tag(COROUTINE_ERROR).e("Coroutine cancelled $e")
                throw e
            } catch (e: Exception) {
                Timber.tag(DB_ERROR).e("Error deleting image from db")
                throw e
            }
        }
    }

    override suspend fun deleteImage(image: ImageDto) {
        withContext(Dispatchers.IO) {
            try {
                ensureActive()
                imageDao.deleteImage(
                    ImageEntity(
                        image.id,
                        image.description,
                        image.altDescription,
                        image.urls.regular,
                        image.user.name,
                        image.user.id,
                        image.user.userName,
                        image.user.portfolioUrl,
                        image.links.user,
                        image.links.downloadLink, 0
                    )
                )
            } catch (e: CancellationException) {
                Timber.tag(COROUTINE_ERROR).e("Coroutine cancelled $e")
                throw e
            } catch (e: Exception) {
                Timber.tag(DB_ERROR).e("Error deleting image from db")
                throw e
            }
        }
    }

    override suspend fun getImagesForSync(): Resource<List<ImageDto>> {
        return try {
            val imageList = imageDao.getImagesForSync()
            if (imageList != null) {
                Resource.Success(data = imageList.map { it.toImageDto() })
            } else {
                Resource.Error(message = "No images found")
            }
        } catch (e: Exception) {
            Resource.Error(message = "Error occurred while fetching images: ${e.localizedMessage}")
        }
    }

    private fun ImageEntity.toImageDto(): ImageDto {
        return ImageDto(
            id = this.id,
            description = this.description,
            altDescription = this.altDescription,
            urls = ImageDto.PhotoUrls(
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
            ),
            links = ImageDto.PhotoLinks(
                user = this.userLink,
                downloadLink = this.downloadLink
            ),
            timestamp = this.timestamp
        )
    }
}