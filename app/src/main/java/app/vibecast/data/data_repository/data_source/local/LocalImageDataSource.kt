package app.vibecast.data.data_repository.data_source.local


import app.vibecast.data.local.db.image.ImageEntity
import app.vibecast.data.remote.network.image.Image
import app.vibecast.domain.entity.ImageDto
import kotlinx.coroutines.flow.Flow

interface LocalImageDataSource {

    fun getImages() : Flow<List<ImageEntity>>

    suspend fun addImage(image : ImageDto)

    suspend fun deleteImage(image: ImageDto)
}