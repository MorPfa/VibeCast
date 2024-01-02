package app.vibecast.data.local.source

import android.util.Log
import app.vibecast.data.TAGS.COROUTINE_ERROR
import app.vibecast.data.TAGS.DB_ERROR
import app.vibecast.data.data_repository.data_source.local.LocalImageDataSource
import app.vibecast.data.local.db.image.ImageDao
import app.vibecast.data.local.db.image.ImageEntity
import app.vibecast.domain.entity.ImageDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException




class LocalImageDataSourceImpl @Inject constructor(
    private val imageDao: ImageDao) : LocalImageDataSource {

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
                Log.e(COROUTINE_ERROR, "Coroutine got cancelled test 2 $e")
                throw e

            } catch (e: Exception) {
                Log.e(DB_ERROR, "Error inserting image into db $e")
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
                Log.e(COROUTINE_ERROR, "Coroutine got cancelled test 3 $e")
                throw e
            }
            catch(e: Exception) {
                Log.e(DB_ERROR, "Error deleting image from db")
                throw e
            }
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
            ), links = ImageDto.PhotoLinks(
                user = this.userLink,
                downloadLink = this.downloadLink
            )
        )
    }
}