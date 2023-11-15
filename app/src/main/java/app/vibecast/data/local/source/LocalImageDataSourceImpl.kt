package app.vibecast.data.local.source

import app.vibecast.data.data_repository.data_source.local.LocalImageDataSource
import app.vibecast.data.local.db.image.ImageDao
import app.vibecast.data.local.db.image.ImageEntity
import app.vibecast.domain.entity.ImageDto
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalImageDataSourceImpl @Inject constructor(private val imageDao: ImageDao) : LocalImageDataSource {

    override fun getImages(): Flow<List<ImageEntity>> = imageDao.getAllImages()

    override suspend fun addImage(image: ImageDto) = imageDao.addImage(
        ImageEntity(image.id, image.description, image.urls.regular, image.user.name, image.user.id, image.user.userName, image.user.portfolioUrl)
    )
//TODO need to trigger api's download endpoint in order to be able to do this
    override suspend fun deleteImage(image: ImageDto) = imageDao.deleteImage(
    ImageEntity(image.id, image.description, image.urls.regular,image.user.name, image.user.id, image.user.userName, image.user.portfolioUrl)
    )
}